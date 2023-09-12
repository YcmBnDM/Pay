
package com.example.components.mq.vender.rocketmq.receive;

import com.example.components.mq.constant.MQVenderCS;
import com.example.components.mq.executor.MQThreadExecutor;
import com.example.components.mq.model.extender.PayOrderReissueMQ;
import com.example.components.mq.vender.IMQMsgReceiver;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

;

/**
 * rocketMQ消息接收器：仅在vender=rocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  支付订单补单（一般用于没有回调的接口，比如微信的条码支付）
 *
 */
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
@ConditionalOnBean(PayOrderReissueMQ.IMQReceiver.class)
@RocketMQMessageListener(topic = PayOrderReissueMQ.MQ_NAME, consumerGroup = PayOrderReissueMQ.MQ_NAME)
public class PayOrderReissueRocketMQReceiver implements IMQMsgReceiver, RocketMQListener<String> {

    @Resource
    private PayOrderReissueMQ.IMQReceiver mqReceiver;

    /** 接收 【 queue 】 类型的消息 **/
    @Override
    public void receiveMsg(String msg){
        mqReceiver.receive(PayOrderReissueMQ.parse(msg));
    }

    @Override
    @Async(MQThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    public void onMessage(String message) {
        this.receiveMsg(message);
    }

}
