package com.example.pay.service;

import com.example.entity.RefundOrder;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/***
 * 退款处理通用逻辑
 *
 */
@Service
@Slf4j
public class RefundOrderProcessService {

    @Resource
    private RefundOrderService refundOrderService;
    @Resource
    private PayMchNotifyService payMchNotifyService;

    /**
     * 根据通道返回的状态，处理退款订单业务
     **/
    public boolean handleRefundOrder4Channel(ChannelRetMsg channelRetMsg, RefundOrder refundOrder) {
        boolean updateOrderSuccess = true; //默认更新成功
        String refundOrderId = refundOrder.getRefundOrderId();
        // 明确退款成功
        if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
            updateOrderSuccess = refundOrderService.updateIng2Success(refundOrderId, channelRetMsg.getChannelOrderId());
            if (updateOrderSuccess) {
                // 通知商户系统
                if (StringUtils.isNotEmpty(refundOrder.getNotifyUrl())) {
                    payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
                }
            }
            //确认失败
        } else if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL) {
            // 更新为失败状态
            updateOrderSuccess = refundOrderService.updateIng2Fail(refundOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            // 通知商户系统
            if (StringUtils.isNotEmpty(refundOrder.getNotifyUrl())) {
                payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
            }
        }
        return updateOrderSuccess;
    }

}
