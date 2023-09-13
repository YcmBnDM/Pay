package com.example.pay.mq;

import com.example.components.mq.model.extender.ResetIsvMchAppInfoConfigMQ;
import com.example.pay.service.ConfigContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 接收MQ消息
 * 业务： 更新服务商/商户/商户应用配置信息；
 */
@Slf4j
@Component
public class ResetIsvMchAppInfoMQReceiver implements ResetIsvMchAppInfoConfigMQ.IMQReceiver {

    @Resource
    private ConfigContextService configContextService;

    @Override
    public void receive(ResetIsvMchAppInfoConfigMQ.MsgPayload payload) {

        if (payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO) {
            this.modifyIsvInfo(payload.getIsvNo());
        } else if (payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO) {
            this.modifyMchInfo(payload.getMchNo());
        } else if (payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP) {
            this.modifyMchApp(payload.getMchNo(), payload.getAppId());
        }

    }

    /**
     * 接收 [商户配置信息] 的消息
     **/
    private void modifyMchInfo(String mchNo) {
        log.info("成功接收 [商户配置信息] 的消息, msg={}", mchNo);
        configContextService.initMchInfoConfigContext(mchNo);
        log.info(" [商户配置信息] 已重置");
    }

    /**
     * 接收 [商户应用支付参数配置信息] 的消息
     **/
    private void modifyMchApp(String mchNo, String appId) {
        log.info("成功接收 [商户应用支付参数配置信息] 的消息, mchNo={}, appId={}", mchNo, appId);
        configContextService.initMchAppConfigContext(mchNo, appId);
        log.info(" [商户应用支付参数配置信息] 已重置");
    }

    /**
     * 重置ISV信息
     **/
    private void modifyIsvInfo(String isvNo) {
        log.info("成功接收 [ISV信息] 重置, msg={}", isvNo);
        configContextService.initIsvConfigContext(isvNo);
        log.info("[ISV信息] 已重置");
    }

}
