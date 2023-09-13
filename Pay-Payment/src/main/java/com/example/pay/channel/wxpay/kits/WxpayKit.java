package com.example.pay.channel.wxpay.kits;

import com.example.constants.CS;
import com.example.model.param.wxpay.WxpayIsvsubMchParams;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ConfigContextQueryService;
import com.example.util.SpringBeansUtil;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import org.apache.commons.lang3.StringUtils;

/*
* 【微信支付】支付通道工具包
*
*/
public class WxpayKit {

    /** 放置 isv特殊信息 **/
    public static void putApiIsvInfo(MchAppConfigContext mchAppConfigContext, BaseWxPayRequest req){

        //不是特约商户， 无需放置此值
        if(!mchAppConfigContext.isIsvsubMch()){
            return ;
        }

        ConfigContextQueryService configContextQueryService = SpringBeansUtil.getBean(ConfigContextQueryService.class);

        WxpayIsvsubMchParams isvsubMchParams =
                (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);

        req.setSubMchId(isvsubMchParams.getSubMchId());
        req.setSubAppId(isvsubMchParams.getSubMchAppId());
    }

    /** 构造服务商 + 商户配置  wxPayConfig **/
    public static WxPayConfig getWxPayConfig(WxServiceWrapper wxServiceWrapper){
        return wxServiceWrapper.getWxPayService().getConfig();
    }

    public static String appendErrCode(String code, String subCode){
        return StringUtils.defaultIfEmpty(subCode, code); //优先： subCode
    }

    public static String appendErrMsg(String msg, String subMsg){

        if(StringUtils.isNotEmpty(msg) && StringUtils.isNotEmpty(subMsg) ){
            return msg + "【" + subMsg + "】";
        }
        return StringUtils.defaultIfEmpty(subMsg, msg);
    }

    public static void commonSetErrInfo(ChannelRetMsg channelRetMsg, WxPayException wxPayException){

        channelRetMsg.setChannelErrCode(appendErrCode( wxPayException.getReturnCode(), wxPayException.getErrCode() ));
        channelRetMsg.setChannelErrMsg(appendErrMsg( "OK".equalsIgnoreCase(wxPayException.getReturnMsg()) ? null : wxPayException.getReturnMsg(), wxPayException.getErrCodeDes() ));

        // 如果仍然为空
        if(StringUtils.isEmpty(channelRetMsg.getChannelErrMsg())){
            channelRetMsg.setChannelErrMsg(StringUtils.defaultIfEmpty(wxPayException.getCustomErrorMsg(), wxPayException.getMessage()));
        }

    }

}
