
package com.example.pay.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.model.param.wxpay.WxpayIsvsubMchParams;
import com.example.pay.channel.IPayOrderCloseService;
import com.example.pay.channel.wxpay.kits.WxpayKit;
import com.example.pay.channel.wxpay.kits.WxpayV3Util;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ConfigContextQueryService;
import com.github.binarywang.wxpay.bean.request.WxPayOrderCloseRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderCloseResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 微信关闭订单
 *
 */
@Service
public class WxpayPayOrderCloseService implements IPayOrderCloseService {

    @Resource
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                WxPayOrderCloseRequest req = new WxPayOrderCloseRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payOrder.getPayOrderId());

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                WxPayOrderCloseResult result = wxPayService.closeOrder(req);

                if("SUCCESS".equals(result.getResultCode())){ //关闭订单成功
                    return ChannelRetMsg.confirmSuccess(null);
                }else if("FAIL".equals(result.getResultCode())){ //关闭订单失败
                    return ChannelRetMsg.confirmFail(); //关闭失败
                }else{
                    return ChannelRetMsg.waiting(); //关闭中
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {   //V3

                String reqUrl;
                JSONObject reqJson = new JSONObject();
                if(mchAppConfigContext.isIsvsubMch()){ // 特约商户
                    WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
                    reqUrl = String.format("/v3/pay/partner/transactions/out-trade-no/%s/close", payOrder.getPayOrderId());
                    reqJson.put("sp_mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                    reqJson.put("sub_mchid", isvsubMchParams.getSubMchId());
                }else {
                    reqUrl = String.format("/v3/pay/transactions/out-trade-no/%s/close", payOrder.getPayOrderId());
                    reqJson.put("mchid", wxServiceWrapper.getWxPayService().getConfig().getMchId());
                }

                WxpayV3Util.closeOrderV3(reqUrl, reqJson, wxServiceWrapper.getWxPayService());
                return ChannelRetMsg.confirmSuccess(null);
            }
            return ChannelRetMsg.confirmFail();
        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

}
