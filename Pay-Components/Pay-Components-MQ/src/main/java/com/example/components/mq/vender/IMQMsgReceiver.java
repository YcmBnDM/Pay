
package com.example.components.mq.vender;

/**
 * MQ 消息接收器 接口定义
 */
public interface IMQMsgReceiver {

    /**
     * 接收消息
     **/
    void receiveMsg(String msg);
}
