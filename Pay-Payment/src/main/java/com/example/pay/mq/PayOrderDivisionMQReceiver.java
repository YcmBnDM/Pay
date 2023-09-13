package com.example.pay.mq;

import com.example.components.mq.model.extender.PayOrderDivisionMQ;
import com.example.pay.service.PayOrderDivisionProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 接收MQ消息
 * 业务： 支付订单分账处理逻辑
 */
@Slf4j
@Component
public class PayOrderDivisionMQReceiver implements PayOrderDivisionMQ.IMQReceiver {

    @Resource
    private PayOrderDivisionProcessService payOrderDivisionProcessService;

    @Override
    public void receive(PayOrderDivisionMQ.MsgPayload payload) {

        try {
            log.info("接收订单分账通知MQ, msg={}", payload.toString());
            payOrderDivisionProcessService.processPayOrderDivision(payload.getPayOrderId(), payload.getUseSysAutoDivisionReceivers(), payload.getReceiverList(), payload.getIsResend());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
