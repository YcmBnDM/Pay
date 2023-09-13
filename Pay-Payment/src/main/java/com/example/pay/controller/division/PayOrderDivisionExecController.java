package com.example.pay.controller.division;

import com.alibaba.fastjson.JSON;
import com.example.components.mq.model.extender.PayOrderDivisionMQ;
import com.example.constants.CS;
import com.example.entity.MchDivisionReceiver;
import com.example.entity.MchDivisionReceiverGroup;
import com.example.entity.PayOrder;
import com.example.entity.PayOrderDivisionRecord;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.controller.ApiController;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.division.PayOrderDivisionExecRQ;
import com.example.pay.rqrs.resp.division.PayOrderDivisionExecRS;
import com.example.pay.service.ConfigContextQueryService;
import com.example.pay.service.PayOrderDivisionProcessService;
import com.example.service.impl.MchDivisionReceiverGroupService;
import com.example.service.impl.MchDivisionReceiverService;
import com.example.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 发起分账请求
 */
@Slf4j
@RestController
public class PayOrderDivisionExecController extends ApiController {

    @Resource
    private ConfigContextQueryService configContextQueryService;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private MchDivisionReceiverService mchDivisionReceiverService;
    @Resource
    private MchDivisionReceiverGroupService mchDivisionReceiverGroupService;
    @Resource
    private PayOrderDivisionProcessService payOrderDivisionProcessService;

    /**
     * 分账执行
     **/
    @PostMapping("/api/division/exec")
    public ApiRes exec() {

        //获取参数 & 验签
        PayOrderDivisionExecRQ bizRQ = getRQByWithMchSign(PayOrderDivisionExecRQ.class);

        try {

            if (StringUtils.isAllEmpty(bizRQ.getMchOrderNo(), bizRQ.getPayOrderId())) {
                throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
            }

            PayOrder payOrder = payOrderService.queryMchOrder(bizRQ.getMchNo(), bizRQ.getPayOrderId(), bizRQ.getMchOrderNo());
            if (payOrder == null) {
                throw new BizException("订单不存在");
            }

            if (payOrder.getState() != PayOrder.STATE_SUCCESS || payOrder.getDivisionState() != PayOrder.DIVISION_STATE_UNHAPPEN || payOrder.getDivisionMode() != PayOrder.DIVISION_MODE_MANUAL) {
                throw new BizException("当前订单状态不支持分账");
            }

            List<PayOrderDivisionMQ.CustomerDivisionReceiver> receiverList = null;

            //不使用默认分组， 需要转换每个账号信息
            if (bizRQ.getUseSysAutoDivisionReceivers() != CS.YES && !StringUtils.isEmpty(bizRQ.getReceivers())) {
                receiverList = JSON.parseArray(bizRQ.getReceivers(), PayOrderDivisionMQ.CustomerDivisionReceiver.class);
            }

            // 验证账号是否合法
            this.checkReceiverList(receiverList, payOrder.getIfCode(), bizRQ.getMchNo(), bizRQ.getAppId());

            // 商户配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(bizRQ.getMchNo(), bizRQ.getAppId());
            if (mchAppConfigContext == null) {
                throw new BizException("获取商户应用信息失败");
            }

            //处理分账请求
            ChannelRetMsg channelRetMsg = payOrderDivisionProcessService.processPayOrderDivision(payOrder.getPayOrderId(), bizRQ.getUseSysAutoDivisionReceivers(), receiverList, false);

            PayOrderDivisionExecRS bizRS = new PayOrderDivisionExecRS();
            bizRS.setState(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS ? PayOrderDivisionRecord.STATE_SUCCESS : PayOrderDivisionRecord.STATE_FAIL);
            bizRS.setChannelBatchOrderId(channelRetMsg.getChannelOrderId());
            bizRS.setErrCode(channelRetMsg.getChannelErrCode());
            bizRS.setErrMsg(channelRetMsg.getChannelErrMsg());

            return ApiRes.okWithSign(bizRS, mchAppConfigContext.getMchApp().getAppSecret());

        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }

    /**
     * 检验账号是否合法
     **/
    private void checkReceiverList(List<PayOrderDivisionMQ.CustomerDivisionReceiver> receiverList, String ifCode, String mchNo, String appId) {

        if (receiverList == null || receiverList.isEmpty()) {
            return;
        }

        Set<Long> receiverIdSet = new HashSet<>();
        Set<Long> receiverGroupIdSet = new HashSet<>();

        for (PayOrderDivisionMQ.CustomerDivisionReceiver receiver : receiverList) {

            if (receiver.getReceiverId() != null) {
                receiverIdSet.add(receiver.getReceiverId());
            }

            if (receiver.getReceiverGroupId() != null) {
                receiverGroupIdSet.add(receiver.getReceiverGroupId());
            }

            if (receiver.getReceiverId() == null && receiver.getReceiverGroupId() == null) {
                throw new BizException("分账用户组： receiverId 和 与receiverGroupId 必填一项");
            }

            if (receiver.getDivisionProfit() != null) {

                if (receiver.getDivisionProfit().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BizException("分账用户receiverId=[" + (receiver.getReceiverId() == null ? "" : receiver.getReceiverId()) + "]," +
                            "receiverGroupId=[" + (receiver.getReceiverGroupId() == null ? "" : receiver.getReceiverGroupId()) + "] 分账比例不得小于0%");
                }

                if (receiver.getDivisionProfit().compareTo(BigDecimal.ONE) > 0) {
                    throw new BizException("分账用户receiverId=[" + (receiver.getReceiverId() == null ? "" : receiver.getReceiverId()) + "]," +
                            "receiverGroupId=[" + (receiver.getReceiverGroupId() == null ? "" : receiver.getReceiverGroupId()) + "] 分账比例不得高于100%");
                }
            }
        }


        if (!receiverIdSet.isEmpty()) {

            int receiverCount = (int) mchDivisionReceiverService.count(MchDivisionReceiver.gw()
                    .in(MchDivisionReceiver::getReceiverId, receiverIdSet)
                    .eq(MchDivisionReceiver::getMchNo, mchNo)
                    .eq(MchDivisionReceiver::getAppId, appId)
                    .eq(MchDivisionReceiver::getIfCode, ifCode)
                    .eq(MchDivisionReceiver::getState, CS.YES)
            );

            if (receiverCount != receiverIdSet.size()) {
                throw new BizException("分账[用户]中包含不存在或渠道不可用账号，请更改");
            }
        }

        if (!receiverGroupIdSet.isEmpty()) {

            int receiverGroupCount = (int) mchDivisionReceiverGroupService.count(MchDivisionReceiverGroup.gw()
                    .in(MchDivisionReceiverGroup::getReceiverGroupId, receiverGroupIdSet)
                    .eq(MchDivisionReceiverGroup::getMchNo, mchNo)
            );

            if (receiverGroupCount != receiverGroupIdSet.size()) {
                throw new BizException("分账[账号组]中包含不存在或不可用组，请更改");
            }
        }

    }


}
