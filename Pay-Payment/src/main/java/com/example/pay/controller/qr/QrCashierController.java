
package com.example.pay.controller.qr;

import com.alipay.api.AlipayApiException;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.channel.IChannelUserService;
import com.example.pay.controller.payorder.AbstractPayOrderController;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliJsapiOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.weixin.WxJsapiOrderRQ;
import com.example.pay.service.ConfigContextQueryService;
import com.example.pay.service.PayMchNotifyService;
import com.example.service.impl.PayOrderService;
import com.example.service.impl.SysConfigService;
import com.example.util.PayUtil;
import com.example.util.SpringBeansUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

;

/*
* 聚合码支付二维码收银台controller
*/
@RestController
@RequestMapping("/api/cashier")
public class QrCashierController extends AbstractPayOrderController {

    @Resource
    private PayOrderService payOrderService;
    @Resource private ConfigContextQueryService configContextQueryService;
    @Resource private SysConfigService sysConfigService;
    @Resource private PayMchNotifyService payMchNotifyService;

    /**
     * 返回 oauth2【获取uerId跳转地址】
     * **/
    @PostMapping("/redirectUrl")
    public ApiRes redirectUrl(){

        //查询订单
        PayOrder payOrder = getPayOrder();

        //回调地址
        String redirectUrlEncode = sysConfigService.getDBApplicationConfig().genOauth2RedirectUrlEncode(payOrder.getPayOrderId());

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

        //获取接口并返回数据
        IChannelUserService channelUserService = getServiceByWayCode(getWayCode(), "ChannelUserService", IChannelUserService.class);
        return ApiRes.ok(channelUserService.buildUserRedirectUrl(redirectUrlEncode, mchAppConfigContext));

    }

    /**
     * 获取userId
     * **/
    @PostMapping("/channelUserId")
    public ApiRes channelUserId() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        String wayCode = getWayCode();

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        IChannelUserService channelUserService = getServiceByWayCode(wayCode, "ChannelUserService", IChannelUserService.class);
        return ApiRes.ok(channelUserService.getChannelUserId(getReqParamJSON(), mchAppConfigContext));

    }


    /**
     * 获取订单支付信息
     * **/
    @PostMapping("/payOrderInfo")
    public ApiRes payOrderInfo() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        PayOrder resOrder = new PayOrder();
        resOrder.setPayOrderId(payOrder.getPayOrderId());
        resOrder.setMchOrderNo(payOrder.getMchOrderNo());
        resOrder.setMchName(payOrder.getMchName());
        resOrder.setAmount(payOrder.getAmount());
        resOrder.setReturnUrl(payMchNotifyService.createReturnUrl(payOrder, configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId()).getMchApp().getAppSecret()));
        return ApiRes.ok(resOrder);
    }


    /** 调起下单接口, 返回支付数据包  **/
    @PostMapping("/pay")
    public ApiRes pay() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        String wayCode = getWayCode();

        ApiRes apiRes = null;

        if(wayCode.equals(CS.PAY_WAY_CODE.ALI_JSAPI)){
            apiRes = packageAlipayPayPackage(payOrder);
        }else if(wayCode.equals(CS.PAY_WAY_CODE.WX_JSAPI)){
            apiRes = packageWxpayPayPackage(payOrder);
        }

        return ApiRes.ok(apiRes);
    }


    /** 获取支付宝的 支付参数 **/
    private ApiRes packageAlipayPayPackage(PayOrder payOrder) throws AlipayApiException {

        String channelUserId = getValStringRequired("channelUserId");
        AliJsapiOrderRQ rq = new AliJsapiOrderRQ();
        rq.setBuyerUserId(channelUserId);
        return this.unifiedOrder(getWayCode(), rq, payOrder);
    }


    /** 获取微信的 支付参数 **/
    private ApiRes packageWxpayPayPackage(PayOrder payOrder) throws AlipayApiException {

        String openId = getValStringRequired("channelUserId");
        WxJsapiOrderRQ rq = new WxJsapiOrderRQ();
        rq.setOpenid(openId);
        return this.unifiedOrder(getWayCode(), rq, payOrder);
    }


    private String getToken(){
        return getValStringRequired("token");
    }

    private String getWayCode(){
        return getValStringRequired("wayCode");
    }

    private PayOrder getPayOrder(){

        String payOrderId = PayUtil.aesDecode(getToken()); //解析token

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if(payOrder == null || payOrder.getState() != PayOrder.STATE_INIT){
            throw new BizException("订单不存在或状态不正确");
        }

        return payOrderService.getById(payOrderId);
    }


    private <T> T getServiceByWayCode(String wayCode, String serviceSuffix, Class<T> cls){

        if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.ALIPAY + serviceSuffix, cls);
        }else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.WXPAY + serviceSuffix, cls);
        }

        return null;
    }






}
