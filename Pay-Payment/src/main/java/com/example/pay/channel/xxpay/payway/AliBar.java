
package com.example.pay.channel.xxpay.payway;

import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.param.xxpay.XxpayNormalMchParams;
import com.example.pay.channel.xxpay.XxpayPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliBarOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliBarOrderRS;
import com.example.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/*
 * 小新支付 支付宝条码支付
 *
 */
@Service("xxpayPaymentByAliBarService") //Service Name需保持全局唯一性
@Slf4j
public class AliBar extends XxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext){
        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;
        XxpayNormalMchParams params = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
        // 构造支付请求参数
        Map<String,Object> paramMap = new TreeMap();
        paramMap.put("mchId", params.getMchId());
        paramMap.put("productId", "8021"); // 支付宝条码
        paramMap.put("mchOrderNo", payOrder.getPayOrderId());
        paramMap.put("amount", payOrder.getAmount() + "");
        paramMap.put("currency", "cny");
        paramMap.put("clientIp", payOrder.getClientIp());
        paramMap.put("device", "web");
        paramMap.put("returnUrl", getReturnUrl());
        paramMap.put("notifyUrl", getNotifyUrl(payOrder.getPayOrderId()));
        paramMap.put("subject", payOrder.getSubject());
        paramMap.put("body", payOrder.getBody());
        paramMap.put("extra", bizRQ.getAuthCode());
        // 构造函数响应数据
        AliBarOrderRS res = ApiResBuilder.buildSuccess(AliBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        // 发起支付
        doPay(payOrder, params, paramMap, channelRetMsg);
        return res;
    }

}
