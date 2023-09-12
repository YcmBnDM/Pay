package com.example.components.mq.model.extender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.components.mq.constant.MQSendTypeEnum;
import com.example.components.mq.model.AbstractMQ;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 定义MQ消息格式
 * 业务场景： [ 清除商户登录信息 ]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanMchLoginAuthCacheMQ extends AbstractMQ {

    /**
     * 【！重要配置项！】 定义MQ名称
     **/
    public static final String MQ_NAME = "QUEUE_CLEAN_MCH_LOGIN_AUTH_CACHE";

    /**
     * 内置msg 消息体定义
     **/
    private MsgPayload payload;

    /**
     * 【！重要配置项！】 定义Msg消息载体
     **/
    @Data
    @AllArgsConstructor
    public static class MsgPayload {

        /**
         * 用户ID集合
         **/
        private List<Long> userIdList;

    }

    @Override
    public String getMQName() {
        return MQ_NAME;
    }

    /**
     * 【！重要配置项！】
     **/
    @Override
    public MQSendTypeEnum getMQType() {
        return MQSendTypeEnum.QUEUE;  // QUEUE - 点对点 、 BROADCAST - 广播模式
    }

    @Override
    public String toMessage() {
        return JSONObject.toJSONString(payload);
    }

    /**
     * 【！重要配置项！】 构造MQModel , 一般用于发送MQ时
     **/
    public static CleanMchLoginAuthCacheMQ build(List<Long> userIdList) {
        return new CleanMchLoginAuthCacheMQ(new MsgPayload(userIdList));
    }

    /**
     * 解析MQ消息， 一般用于接收MQ消息时
     **/
    public static MsgPayload parse(String msg) {
        return JSON.parseObject(msg, MsgPayload.class);
    }

    /**
     * 定义 IMQReceiver 接口： 项目实现该接口则可接收到对应的业务消息
     **/
    public interface IMQReceiver {
        void receive(MsgPayload payload);
    }

}
