package com.example.pay.mq;

import com.example.components.mq.model.extender.PayOrderReissueMQ;
import com.example.components.mq.vender.IMQSender;
import com.example.entity.PayOrder;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ChannelOrderReissueService;
import com.example.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 接收MQ消息
 * 业务： 支付订单补单（一般用于没有回调的接口，比如微信的条码支付）
 */
@Slf4j
@Component
public class PayOrderReissueMQReceiver implements PayOrderReissueMQ.IMQReceiver {

    @Resource
    private IMQSender mqSender;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private ChannelOrderReissueService channelOrderReissueService;


    @Override
    public void receive(PayOrderReissueMQ.MsgPayload payload) {
        try {
            String payOrderId = payload.getPayOrderId();
            int currentCount = payload.getCount();
            log.info("接收轮询查单通知MQ, payOrderId={}, count={}", payOrderId, currentCount);
            currentCount++;

            PayOrder payOrder = payOrderService.getById(payOrderId);
            if (payOrder == null) {
                log.warn("查询支付订单为空,payOrderId={}", payOrderId);
                return;
            }

            if (payOrder.getState() != PayOrder.STATE_ING) {
                log.warn("订单状态不是支付中,不需查询渠道.payOrderId={}", payOrderId);
                return;
            }

            ChannelRetMsg channelRetMsg = channelOrderReissueService.processPayOrder(payOrder);

            //返回null 可能为接口报错等， 需要再次轮询
            if (channelRetMsg == null || channelRetMsg.getChannelState() == null || channelRetMsg.getChannelState().equals(ChannelRetMsg.ChannelState.WAITING)) {

                //最多查询6次
                if (currentCount <= 6) {
                    mqSender.send(PayOrderReissueMQ.build(payOrderId, currentCount), 5); //延迟5s再次查询
                } else {

                    //TODO 调用【撤销订单】接口

                }

            } else { //其他状态， 不需要再次轮询。
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
    }
}
