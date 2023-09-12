
package com.example.components.mq.vender.rocketmq.receive;

import com.example.components.mq.constant.MQVenderCS;
import com.example.components.mq.model.extender.CleanMchLoginAuthCacheMQ;
import com.example.components.mq.vender.IMQMsgReceiver;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

;

/**
 * rocketMQ消息接收器：仅在vender=rocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
 *      业务：  清除商户登录信息
 */
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
@ConditionalOnBean(CleanMchLoginAuthCacheMQ.IMQReceiver.class)
@RocketMQMessageListener(topic = CleanMchLoginAuthCacheMQ.MQ_NAME, consumerGroup = CleanMchLoginAuthCacheMQ.MQ_NAME)
public class CleanMchLoginAuthCacheRocketMQReceiver implements IMQMsgReceiver, RocketMQListener<String> {

    @Resource
    private CleanMchLoginAuthCacheMQ.IMQReceiver mqReceiver;

    /**
     * 接收 【 queue 】 类型的消息
     **/
    @Override
    public void receiveMsg(String msg) {
        mqReceiver.receive(CleanMchLoginAuthCacheMQ.parse(msg));
    }

    @Override
    public void onMessage(String message) {
        this.receiveMsg(message);
    }

}
