
package com.example.pay.channel.wxpay.paywayV3;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.wxpay.WxpayPaymentService;
import com.example.pay.channel.wxpay.kits.WxpayKit;
import com.example.pay.channel.wxpay.kits.WxpayV3Util;
import com.example.pay.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.weixin.WxNativeOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.weixin.WxNativeOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.springframework.stereotype.Service;

/*
 * 微信 native支付
 *
 */
@Service("wxpayPaymentByNativeV3Service") //Service Name需保持全局唯一性
public class WxNative extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        WxNativeOrderRQ bizRQ = (WxNativeOrderRQ) rq;

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 构造函数响应数据
        WxNativeOrderRS res = ApiResBuilder.buildSuccess(WxNativeOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        try {
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvsubMch(), WxPayConstants.TradeType.NATIVE, null);

            JSONObject resJSON = JSONObject.parseObject(payInfo);

            String codeUrl = resJSON.getString("code_url");
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码图片地址

                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(codeUrl));
            }else{

                res.setCodeUrl(codeUrl);
            }

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
