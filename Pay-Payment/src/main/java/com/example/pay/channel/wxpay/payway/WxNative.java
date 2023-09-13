
package com.example.pay.channel.wxpay.payway;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.wxpay.WxpayPaymentService;
import com.example.pay.channel.wxpay.kits.WxpayKit;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.weixin.WxNativeOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.weixin.WxNativeOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.springframework.stereotype.Service;

/*
 * 微信 native支付
 */
@Service("wxpayPaymentByNativeService") //Service Name需保持全局唯一性
public class WxNative extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxNativeOrderRQ bizRQ = (WxNativeOrderRQ) rq;

        WxPayUnifiedOrderRequest req = buildUnifiedOrderRequest(payOrder, mchAppConfigContext);
        req.setTradeType(WxPayConstants.TradeType.NATIVE);

        // 构造函数响应数据
        WxNativeOrderRS res = ApiResBuilder.buildSuccess(WxNativeOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayNativeOrderResult wxPayNativeOrderResult = wxPayService.createOrder(req);

            String codeUrl = wxPayNativeOrderResult.getCodeUrl();
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码图片地址

                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(codeUrl));
            }else{

                res.setCodeUrl(codeUrl);
            }

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
