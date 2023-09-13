package com.example.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.components.mq.model.extender.PayOrderMchNotifyMQ;
import com.example.components.mq.vender.IMQSender;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.MchNotifyRecord;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.impl.MchNotifyRecordService;
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
 * 商户通知类
 */
@Api(tags = "订单管理（通知类）")
@RestController
@RequestMapping("/api/mchNotify")
public class MchNotifyController extends CommonCtrl {

    @Resource
    private MchNotifyRecordService mchNotifyService;
    @Resource
    private IMQSender mqSender;

    /**
     * @describe: 商户通知列表
     */
    @ApiOperation("查询商户通知列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"), @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"), @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"), @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"), @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"), @ApiImplicitParam(name = "mchNo", value = "商户号"), @ApiImplicitParam(name = "orderId", value = "订单ID"), @ApiImplicitParam(name = "mchOrderNo", value = "商户订单号"), @ApiImplicitParam(name = "isvNo", value = "服务商号"), @ApiImplicitParam(name = "appId", value = "应用ID"), @ApiImplicitParam(name = "state", value = "通知状态,1-通知中,2-通知成功,3-通知失败", dataType = "Byte"), @ApiImplicitParam(name = "orderType", value = "订单类型:1-支付,2-退款", dataType = "Byte")})
    @PreAuthorize("hasAuthority('ENT_NOTIFY_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<MchNotifyRecord> list() {

        MchNotifyRecord mchNotify = getObject(MchNotifyRecord.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<MchNotifyRecord> wrapper = MchNotifyRecord.gw();
        if (StringUtils.isNotEmpty(mchNotify.getOrderId())) {
            wrapper.eq(MchNotifyRecord::getOrderId, mchNotify.getOrderId());
        }
        if (StringUtils.isNotEmpty(mchNotify.getMchNo())) {
            wrapper.eq(MchNotifyRecord::getMchNo, mchNotify.getMchNo());
        }
        if (StringUtils.isNotEmpty(mchNotify.getIsvNo())) {
            wrapper.eq(MchNotifyRecord::getIsvNo, mchNotify.getIsvNo());
        }
        if (StringUtils.isNotEmpty(mchNotify.getMchOrderNo())) {
            wrapper.eq(MchNotifyRecord::getMchOrderNo, mchNotify.getMchOrderNo());
        }
        if (mchNotify.getOrderType() != null) {
            wrapper.eq(MchNotifyRecord::getOrderType, mchNotify.getOrderType());
        }
        if (mchNotify.getState() != null) {
            wrapper.eq(MchNotifyRecord::getState, mchNotify.getState());
        }
        if (StringUtils.isNotEmpty(mchNotify.getAppId())) {
            wrapper.eq(MchNotifyRecord::getAppId, mchNotify.getAppId());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
        IPage<MchNotifyRecord> pages = mchNotifyService.page(getIPage(), wrapper);

        return ApiPageRes.pages(pages);
    }

    /**
     * @describe: 商户通知信息
     */
    @ApiOperation("通知信息详情")
    @ApiImplicitParams({@ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"), @ApiImplicitParam(name = "notifyId", value = "商户通知记录ID", required = true)})
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_VIEW')")
    @RequestMapping(value = "/{notifyId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("notifyId") String notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(mchNotify);
    }

    /*
     * 功能描述: 商户通知重发操作
     */
    @ApiOperation("商户通知重发")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "iToken",
                            value = "用户身份凭证",
                            required = true,
                            paramType = "header"),
                    @ApiImplicitParam(name = "notifyId",
                            value = "商户通知记录ID",
                            required = true)})
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_RESEND')")
    @RequestMapping(value = "resend/{notifyId}", method = RequestMethod.POST)
    public ApiRes resend(@PathVariable("notifyId") Long notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (mchNotify.getState() != MchNotifyRecord.STATE_FAIL) {
            throw new BizException("请选择失败的通知记录");
        }

        //更新通知中
        mchNotifyService.getBaseMapper().updateIngAndAddNotifyCountLimit(notifyId);

        //调起MQ重发
        mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        return ApiRes.ok(mchNotify);
    }

}
