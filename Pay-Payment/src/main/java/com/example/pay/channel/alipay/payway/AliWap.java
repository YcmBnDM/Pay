package com.example.pay.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.alipay.AlipayKit;
import com.example.pay.channel.alipay.AlipayPaymentService;
import com.example.pay.exception.ChannelException;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliWapOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliWapOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.example.util.other.AmountUtil;
import org.springframework.stereotype.Service;

/**
 * 支付宝 wap支付
 */
@Service("alipayPaymentByAliWapService") //Service Name需保持全局唯一性
public class AliWap extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AliWapOrderRQ bizRQ = (AliWapOrderRQ) rq;

        AlipayTradeWapPayRequest req = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountUtil.convertCentToDollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setProductCode("QUICK_WAP_PAY");
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setReturnUrl(getReturnUrl()); // 同步跳转地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        // 构造函数响应数据
        AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);

        try {
            if (CS.PAY_DATA_TYPE.FORM.equals(bizRQ.getPayDataType())) { //表单方式
                res.setFormContent(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody());

            } else if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())) { //二维码图片地址

                String payUrl = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req, "GET").getBody();
                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(payUrl));
            } else { // 默认都为 payUrl方式

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
