package com.example.pay.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.alipay.AlipayKit;
import com.example.pay.channel.alipay.AlipayPaymentService;
import com.example.pay.exception.ChannelException;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliPcOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliPcOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.example.util.other.AmountUtil;
import org.springframework.stereotype.Service;

/**
 * 支付宝 PC支付
 */
@Service("alipayPaymentByAliPcService") //Service Name需保持全局唯一性
public class AliPc extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AliPcOrderRQ bizRQ = (AliPcOrderRQ) rq;

        AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountUtil.convertCentToDollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setQrPayMode("2"); //订单码-跳转模式
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setReturnUrl(getReturnUrl()); // 同步跳转地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        // 构造函数响应数据
        AliPcOrderRS res = ApiResBuilder.buildSuccess(AliPcOrderRS.class);

        try {
            if (CS.PAY_DATA_TYPE.FORM.equals(bizRQ.getPayDataType())) {
                res.setFormContent(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody());
            } else {
                res.setPayUrl(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req, "GET").getBody());
            }
        } catch (AlipayApiException e) {
            throw ChannelException.sysError(e.getMessage());
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        return res;
    }

}
