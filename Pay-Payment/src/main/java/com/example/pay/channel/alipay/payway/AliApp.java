package com.example.pay.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.example.entity.PayOrder;
import com.example.pay.channel.alipay.AlipayKit;
import com.example.pay.channel.alipay.AlipayPaymentService;
import com.example.pay.exception.ChannelException;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliAppOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.example.util.other.AmountUtil;
import org.springframework.stereotype.Service;

/**
 * 支付宝 APP支付
 */
@Service("alipayPaymentByAliAppService") //Service Name需保持全局唯一性
public class AliApp extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AlipayTradeAppPayRequest req = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountUtil.convertCentToDollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);


        String payData = null;

        // sdk方式需自行拦截接口异常信息
        try {
            payData = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().sdkExecute(req).getBody();
        } catch (AlipayApiException e) {
            throw ChannelException.sysError(e.getMessage());
        }

        // 构造函数响应数据
        AliAppOrderRS res = ApiResBuilder.buildSuccess(AliAppOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(payData);
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        res.setPayData(payData);
        return res;
    }

}
