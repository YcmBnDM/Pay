package com.example.pay.channel.plspay.payway;

import com.alibaba.fastjson.JSONObject;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.param.plspay.PlspayConfig;
import com.example.pay.channel.plspay.PlspayKit;
import com.example.pay.channel.plspay.PlspayPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.weixin.WxJsapiOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.weixin.WxJsapiOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 计全付 微信 jsapi支付
 *
 */
@Service("plspayPaymentByWxJsapiService") //Service Name需保持全局唯一性
@Slf4j
public class WxJsapi extends PlspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        if (StringUtils.isEmpty(bizRQ.getOpenid())) {
            throw new BizException("[openid]不可为空");
        }
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;

        // 构造函数响应数据
        WxJsapiOrderRS res = ApiResBuilder.buildSuccess(WxJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            // 构建请求数据
            PayOrderCreateReqModel model = new PayOrderCreateReqModel();
            // 支付方式
            model.setWayCode(PlspayConfig.WX_JSAPI);
            // 异步通知地址
            model.setNotifyUrl(getNotifyUrl());
            JSONObject channelExtra = new JSONObject();
            channelExtra.put("openid", bizRQ.getOpenid());
            // 微信openId
            model.setChannelExtra(channelExtra.toString());

            // 发起统一下单
            PayOrderCreateResponse response = PlspayKit.payRequest(payOrder, mchAppConfigContext, model);
            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            if (isSuccess) {
                // 下单成功
                res.setPayInfo(response.getData().getString("payData"));

                channelRetMsg.setChannelOrderId(response.get().getPayOrderId());
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(response.getCode()+"");
                channelRetMsg.setChannelErrMsg(response.getMsg());
            }
        } catch (JeepayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }
        return res;
    }
}
