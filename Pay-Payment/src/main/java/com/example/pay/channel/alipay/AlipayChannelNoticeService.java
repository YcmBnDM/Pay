package com.example.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.exception.ResponseException;
import com.example.model.param.alipay.AlipayConfig;
import com.example.model.param.alipay.AlipayIsvParams;
import com.example.model.param.alipay.AlipayNormalMchParams;
import com.example.pay.channel.AbstractChannelNoticeService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付宝 回调接口实现类
 */
@Service
@Slf4j
public class AlipayChannelNoticeService extends AbstractChannelNoticeService {


    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("out_trade_no");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }


    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
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

            boolean verifyResult;
            if (useCert != null && useCert == CS.YES) {  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            } else {
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if (!verifyResult) {
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("trade_no")); //渠道订单号
            result.setChannelUserId(jsonParams.getString("buyer_id")); //支付用户ID
            result.setResponseEntity(okResponse); //响应数据

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            if ("TRADE_SUCCESS".equals(jsonParams.getString("trade_status"))) {
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

            } else if ("TRADE_CLOSED".equals(jsonParams.getString("trade_status"))) {
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);

            }

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
