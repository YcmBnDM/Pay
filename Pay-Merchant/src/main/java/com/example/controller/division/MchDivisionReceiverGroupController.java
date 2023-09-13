package com.example.controller.division;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.aop.MethodLog;
import com.example.constants.ApiCodeEnum;
import com.example.constants.CS;
import com.example.controller.CommonCtrl;
import com.example.entity.MchDivisionReceiver;
import com.example.entity.MchDivisionReceiverGroup;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.impl.MchDivisionReceiverGroupService;
import com.example.service.impl.MchDivisionReceiverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

;

/**
 * 商户分账接收者账号组
 */
@Api(tags = "分账管理（账号组）")
@RestController
@RequestMapping("api/divisionReceiverGroups")
public class MchDivisionReceiverGroupController extends CommonCtrl {

    @Resource
    private MchDivisionReceiverGroupService mchDivisionReceiverGroupService;
    @Resource
    private MchDivisionReceiverService mchDivisionReceiverService;

    /**
     * list
     */
    @ApiOperation("账号组列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "receiverGroupId", value = "账号组ID", dataType = "Long"),
            @ApiImplicitParam(name = "receiverGroupName", value = "组名称")
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_DIVISION_RECEIVER_GROUP_LIST' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<MchDivisionReceiverGroup> list() {

        MchDivisionReceiverGroup queryObject = getObject(MchDivisionReceiverGroup.class);

        LambdaQueryWrapper<MchDivisionReceiverGroup> condition = MchDivisionReceiverGroup.gw();
        condition.eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo());

        if (StringUtils.isNotEmpty(queryObject.getReceiverGroupName())) {
            condition.like(MchDivisionReceiverGroup::getReceiverGroupName, queryObject.getReceiverGroupName());
        }

        if (queryObject.getReceiverGroupId() != null) {
            condition.eq(MchDivisionReceiverGroup::getReceiverGroupId, queryObject.getReceiverGroupId());
        }

        condition.orderByDesc(MchDivisionReceiverGroup::getCreatedAt); //时间倒序

        IPage<MchDivisionReceiverGroup> pages = mchDivisionReceiverGroupService.page(getIPage(true), condition);
        return ApiPageRes.pages(pages);
    }


    /**
     * detail
     */
    @ApiOperation("账号组详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "账号组ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_GROUP_VIEW' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") Long recordId) {
        MchDivisionReceiverGroup record = mchDivisionReceiverGroupService
                .getOne(MchDivisionReceiverGroup.gw()
                        .eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo())
                        .eq(MchDivisionReceiverGroup::getReceiverGroupId, recordId));
        if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(record);
    }

    /**
     * add
     */
    @ApiOperation("新增分账账号组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "autoDivisionFlag", value = "自动分账组（当订单分账模式为自动分账，改组将完成分账逻辑） 0-否 1-是", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "receiverGroupName", value = "组名称", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_GROUP_ADD' )")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @MethodLog(remark = "新增分账账号组")
    public ApiRes add() {
        MchDivisionReceiverGroup record = getObject(MchDivisionReceiverGroup.class);
        record.setMchNo(getCurrentMchNo());
        record.setCreatedUid(getCurrentUser().getSysUser().getSysUserId());
        record.setCreatedBy(getCurrentUser().getSysUser().getRealname());
        if (mchDivisionReceiverGroupService.save(record)) {

            //更新其他组为非默认分账组
            if (record.getAutoDivisionFlag() == CS.YES) {
                mchDivisionReceiverGroupService.update(new LambdaUpdateWrapper<MchDivisionReceiverGroup>()
                        .set(MchDivisionReceiverGroup::getAutoDivisionFlag, CS.NO)
                        .eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo())
                        .ne(MchDivisionReceiverGroup::getReceiverGroupId, record.getReceiverGroupId())
                );
            }
        }
        return ApiRes.ok();
    }

    /**
     * update
     */
    @ApiOperation("更新分账账号组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "账号组ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "autoDivisionFlag", value = "自动分账组（当订单分账模式为自动分账，改组将完成分账逻辑） 0-否 1-是", required = true, dataType = "Byte"),
            @ApiImplicitParam(name = "receiverGroupName", value = "组名称", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_GROUP_EDIT' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.PUT)
    @MethodLog(remark = "更新分账账号组")
    public ApiRes update(@PathVariable("recordId") Long recordId) {

        MchDivisionReceiverGroup reqRecord = getObject(MchDivisionReceiverGroup.class);

        MchDivisionReceiverGroup record = new MchDivisionReceiverGroup();
        record.setReceiverGroupName(reqRecord.getReceiverGroupName());
        record.setAutoDivisionFlag(reqRecord.getAutoDivisionFlag());

        LambdaUpdateWrapper<MchDivisionReceiverGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MchDivisionReceiverGroup::getReceiverGroupId, recordId);
        updateWrapper.eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo());

        if (mchDivisionReceiverGroupService.update(record, updateWrapper)) {

            //更新其他组为非默认分账组
            if (record.getAutoDivisionFlag() == CS.YES) {
                mchDivisionReceiverGroupService.update(new LambdaUpdateWrapper<MchDivisionReceiverGroup>()
                        .set(MchDivisionReceiverGroup::getAutoDivisionFlag, CS.NO)
                        .eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo())
                        .ne(MchDivisionReceiverGroup::getReceiverGroupId, recordId)
                );
            }
        }

        return ApiRes.ok();
    }

    /**
     * delete
     */
    @ApiOperation("删除分账账号组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "账号组ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority('ENT_DIVISION_RECEIVER_GROUP_DELETE')")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.DELETE)
    @MethodLog(remark = "删除分账账号组")
    public ApiRes del(@PathVariable("recordId") Long recordId) {
        MchDivisionReceiverGroup record = mchDivisionReceiverGroupService.getOne(MchDivisionReceiverGroup.gw()
                .eq(MchDivisionReceiverGroup::getReceiverGroupId, recordId).eq(MchDivisionReceiverGroup::getMchNo, getCurrentMchNo()));
        if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        if (mchDivisionReceiverService.count(MchDivisionReceiver.gw().eq(MchDivisionReceiver::getReceiverGroupId, recordId)) > 0) {
            throw new BizException("该组存在账号，无法删除");
        }

        mchDivisionReceiverGroupService.removeById(recordId);
        return ApiRes.ok();
    }


}
