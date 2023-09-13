package com.example.mq;

import com.example.components.mq.model.extender.ResetAppConfigMQ;
import com.example.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 接收MQ消息
 * 业务： 更新系统配置参数
 */
@Slf4j
@Component
public class ResetAppConfigMQReceiver implements ResetAppConfigMQ.IMQReceiver {

    @Resource
    private SysConfigService sysConfigService;

    @Override
    public void receive(ResetAppConfigMQ.MsgPayload payload) {

        log.info("成功接收更新系统配置的订阅通知, msg={}", payload);
        sysConfigService.initDBConfig(payload.getGroupKey());
        log.info("系统配置静态属性已重置");
    }
}
