package com.example.pay.channel.wxpay.payway;

import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.pay.channel.wxpay.WxpayPaymentService;
import com.example.pay.channel.wxpay.kits.WxpayKit;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.weixin.WxBarOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.weixin.WxBarOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 微信 bar
 *
 */
@Service("wxpayPaymentByBarService") //Service Name需保持全局唯一性
public class WxBar extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;

        // 微信统一下单请求对象
        WxPayMicropayRequest request = new WxPayMicropayRequest();
        request.setOutTradeNo(payOrder.getPayOrderId());
        request.setBody(payOrder.getSubject());
        request.setDetail(payOrder.getBody());
        request.setFeeType("CNY");
        request.setTotalFee(payOrder.getAmount().intValue());
        request.setSpbillCreateIp(payOrder.getClientIp());
        request.setAuthCode(bizRQ.getAuthCode().trim());

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            request.setProfitSharing("Y");
        }

        //放置isv信息
        WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

        // 构造函数响应数据
        WxBarOrderRS res = ApiResBuilder.buildSuccess(WxBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayMicropayResult wxPayMicropayResult = wxPayService.micropay(request);

            channelRetMsg.setChannelOrderId(wxPayMicropayResult.getTransactionId());
            channelRetMsg.setChannelUserId(wxPayMicropayResult.getOpenid());
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

        } catch (WxPayException e) {
            //微信返回支付状态为【支付结果未知】, 需进行查单操作
            if("SYSTEMERROR".equals(e.getErrCode()) || "USERPAYING".equals(e.getErrCode()) ||  "BANKERROR".equals(e.getErrCode())){

                //轮询查询订单
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                channelRetMsg.setNeedQuery(true);
            }else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                WxpayKit.commonSetErrInfo(channelRetMsg, e);
            }
        }

        return res;
    }

}
