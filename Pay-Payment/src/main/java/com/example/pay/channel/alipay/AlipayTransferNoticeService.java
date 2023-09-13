package com.example.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.constants.CS;
import com.example.entity.TransferOrder;
import com.example.exception.ResponseException;
import com.example.model.param.alipay.AlipayConfig;
import com.example.model.param.alipay.AlipayIsvParams;
import com.example.model.param.alipay.AlipayNormalMchParams;
import com.example.pay.channel.AbstractTransferNoticeService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付宝 转账回调接口实现类
 */
@Service
@Slf4j
public class AlipayTransferNoticeService extends AbstractTransferNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId) {

        try {

            JSONObject params = getReqParamJSON();
            log.info("【支付宝转账】回调通知参数：{}", params.toJSONString());

            JSONObject bizContent = JSONObject.parseObject(params.getString("biz_content"));

            String transferId = bizContent.getString("out_biz_no");
            return MutablePair.of(transferId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }


    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {

        String logPrefix = "【支付宝转账通知】";

        try {
            //配置参数获取
            Byte useCert = null;
            String alipaySignType, alipayPublicCert, alipayPublicKey = null;
            if (mchAppConfigContext.isIsvsubMch()) {

                // 获取支付参数
                AlipayIsvParams alipayParams = (AlipayIsvParams) configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());
                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();

            } else {

                // 获取支付参数
                AlipayNormalMchParams alipayParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();
            }

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            JSONObject bizContent = JSONObject.parseObject(jsonParams.getString("biz_content"));

            boolean verifyResult;
            if (useCert != null && useCert == CS.YES) {  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            } else {
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if (!verifyResult) {
                log.error("{}，验签失败", logPrefix);
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg channelRetMsg = new ChannelRetMsg();
            channelRetMsg.setResponseEntity(okResponse); // 响应数据

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认转账中

            // 成功－SUCCESS
            String status = bizContent.getString("status");
            if ("SUCCESS".equals(status)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }

            return channelRetMsg;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
