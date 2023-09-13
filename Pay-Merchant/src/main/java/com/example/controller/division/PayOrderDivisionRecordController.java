package com.example.controller.division;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.components.mq.model.extender.PayOrderDivisionMQ;
import com.example.components.mq.vender.IMQSender;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.PayOrderDivisionRecord;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.impl.PayOrderDivisionRecordService;
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
 * 分账记录
 */
@Api(tags = "分账管理（分账记录）")
@RestController
@RequestMapping("api/division/records")
public class PayOrderDivisionRecordController extends CommonCtrl {

    @Resource
    private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Resource
    private IMQSender mqSender;


    /**
     * list
     */
    @ApiOperation("分账记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @ApiImplicitParam(name = "appId", value = "应用ID"),
            @ApiImplicitParam(name = "receiverId", value = "账号快照》 分账接收者ID", dataType = "Long"),
            @ApiImplicitParam(name = "state", value = "状态: 0-待分账 1-分账成功, 2-分账失败", dataType = "Byte"),
            @ApiImplicitParam(name = "receiverGroupId", value = "账号组ID", dataType = "Long"),
            @ApiImplicitParam(name = "accNo", value = "账号快照》 分账接收账号"),
            @ApiImplicitParam(name = "payOrderId", value = "系统支付订单号")
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_DIVISION_RECORD_LIST' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<PayOrderDivisionRecord> list() {

        PayOrderDivisionRecord queryObject = getObject(PayOrderDivisionRecord.class);
        JSONObject paramJSON = getReqParamJSON();

        LambdaQueryWrapper<PayOrderDivisionRecord> condition = PayOrderDivisionRecord.gw();
        condition.eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo());

        if (queryObject.getReceiverId() != null) {
            condition.eq(PayOrderDivisionRecord::getReceiverId, queryObject.getReceiverId());
        }

        if (queryObject.getReceiverGroupId() != null) {
            condition.eq(PayOrderDivisionRecord::getReceiverGroupId, queryObject.getReceiverGroupId());
        }

        if (StringUtils.isNotEmpty(queryObject.getAppId())) {
            condition.like(PayOrderDivisionRecord::getAppId, queryObject.getAppId());
        }

        if (queryObject.getState() != null) {
            condition.eq(PayOrderDivisionRecord::getState, queryObject.getState());
        }

        if (StringUtils.isNotEmpty(queryObject.getPayOrderId())) {
            condition.eq(PayOrderDivisionRecord::getPayOrderId, queryObject.getPayOrderId());
        }

        if (StringUtils.isNotEmpty(queryObject.getAccNo())) {
            condition.eq(PayOrderDivisionRecord::getAccNo, queryObject.getAccNo());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                condition.ge(PayOrderDivisionRecord::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                condition.le(PayOrderDivisionRecord::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }

        condition.orderByDesc(PayOrderDivisionRecord::getCreatedAt); //时间倒序

        IPage<PayOrderDivisionRecord> pages = payOrderDivisionRecordService.page(getIPage(true), condition);
        return ApiPageRes.pages(pages);
    }


    /**
     * detail
     */
    @ApiOperation("分账记录详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "分账记录ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECORD_VIEW' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") Long recordId) {
        PayOrderDivisionRecord record = payOrderDivisionRecordService
                .getOne(PayOrderDivisionRecord.gw()
                        .eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo())
                        .eq(PayOrderDivisionRecord::getRecordId, recordId));
        if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(record);
    }


    /**
     * 分账接口重试
     */
    @ApiOperation("分账接口重试")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "分账记录ID", required = true, dataType = "Long")
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECORD_RESEND' )")
    @RequestMapping(value = "/resend/{recordId}", method = RequestMethod.POST)
    public ApiRes resend(@PathVariable("recordId") Long recordId) {
        PayOrderDivisionRecord record = payOrderDivisionRecordService
                .getOne(PayOrderDivisionRecord.gw()
                        .eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo())
                        .eq(PayOrderDivisionRecord::getRecordId, recordId));
        if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        if (record.getState() != PayOrderDivisionRecord.STATE_FAIL) {
            throw new BizException("请选择失败的分账记录");
        }

        // 更新订单状态 & 记录状态
        payOrderDivisionRecordService.updateResendState(record.getPayOrderId());

        // 重发到MQ
        mqSender.send(PayOrderDivisionMQ.build(record.getPayOrderId(), null, null, true));

        return ApiRes.ok(record);
    }


}
