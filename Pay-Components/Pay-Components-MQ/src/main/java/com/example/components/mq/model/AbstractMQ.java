package com.example.components.mq.model;

import com.example.components.mq.constant.MQSendTypeEnum;

/**
 * 定义了MQ消息格式
 */
public abstract class AbstractMQ {

    /**
     * MQ名称
     **/
    public abstract String getMQName();

    /**
     * MQ 类型
     **/
    public abstract MQSendTypeEnum getMQType();

    /**
     * 构造MQ消息体 String类型
     **/
    public abstract String toMessage();
}
