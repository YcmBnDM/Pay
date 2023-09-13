
package com.example.pay.channel.xxpay;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.RefundOrder;
import com.example.exception.ResponseException;
import com.example.model.param.xxpay.XxpayNormalMchParams;
import com.example.pay.channel.AbstractChannelRefundNoticeService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/*
* 小新支付 退款回调接口实现类
*
*/
@Service
@Slf4j
public class XxpayChannelRefundNoticeService extends AbstractChannelRefundNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String refundOrderId = params.getString("mchRefundNo");
            return MutablePair.of(refundOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            XxpayNormalMchParams xxpayParams = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            String checkSign = jsonParams.getString("sign");
            jsonParams.remove("sign");
            // 验证签名
            if(!checkSign.equals(XxpayKit.getSign(jsonParams, xxpayParams.getKey()))) {
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");

            // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            String status = jsonParams.getString("status");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("channelOrderNo")); //渠道订单号
            result.setResponseEntity(okResponse); //响应数据

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            if("2".equals(status) || "3".equals(status)){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
