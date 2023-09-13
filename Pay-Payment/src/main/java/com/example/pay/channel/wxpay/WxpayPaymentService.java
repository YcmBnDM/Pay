
package com.example.pay.channel.wxpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.param.wxpay.WxpayIsvsubMchParams;
import com.example.pay.channel.AbstractPaymentService;
import com.example.pay.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.model.WxServiceWrapper;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.util.PaywayUtil;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
* 支付接口： 微信官方
* 支付方式： 自适应
*
*/
@Service
public class WxpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        // 微信API版本

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        String apiVersion = wxServiceWrapper.getApiVersion();
        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else if (CS.PAY_IF_VERSION.WX_V3.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayV3Service(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else {
            throw new BizException("不支持的微信支付API版本");
        }

    }

    /**
     * 构建微信统一下单请求数据
     * @param payOrder
     * @return
     */
    public WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setOutTradeNo(payOrderId);
        request.setBody(payOrder.getSubject());
        request.setDetail(payOrder.getBody());
        request.setFeeType("CNY");
        request.setTotalFee(payOrder.getAmount().intValue());
        request.setSpbillCreateIp(payOrder.getClientIp());
        request.setNotifyUrl(getNotifyUrl());
        request.setProductId(System.currentTimeMillis()+"");
        request.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.PURE_DATETIME_PATTERN));

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            request.setProfitSharing("Y");
        }

        // 特约商户
        if(mchAppConfigContext.isIsvsubMch()){
            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
            request.setSubMchId(isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                request.setSubAppId(isvsubMchParams.getSubMchAppId());
            }
        }

        return request;
    }

    /**
     * 构建微信APIV3接口  统一下单请求数据
     * @author terrfly
     * @param payOrder
     * @return
     */
    public WxpayV3OrderRequestModel buildV3OrderRequestModel(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {


        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        WxpayV3OrderRequestModel result = new WxpayV3OrderRequestModel();
        result.setOutTradeNo(payOrderId);
        result.setDescription(payOrder.getSubject());

        // 订单失效时间，遵循rfc3339标准格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE,示例值：2018-06-08T10:34:56+08:00
        result.setTimeExpire(String.format("%sT%s+08:00", DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATE_FORMAT), DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_TIME_FORMAT)));

        result.setNotifyUrl(getNotifyUrl(payOrderId));

        // 订单金额
        result.setAmount(new WxpayV3OrderRequestModel.Amount().setCurrency("CNY").setTotal(payOrder.getAmount().intValue()));

        // 场景信息
        result.setSceneInfo(new WxpayV3OrderRequestModel.SceneInfo().setPayerClientIp(payOrder.getClientIp()));

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            result.setSettleInfo(new WxpayV3OrderRequestModel.SettleInfo().setProfitSharing(true));
        }

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();

        if(mchAppConfigContext.isIsvsubMch()){ // 特约商户

            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

            // 服务商相关参数
            result.setSpAppid(wxPayService.getConfig().getAppId());
            result.setSpMchid(wxPayService.getConfig().getMchId());
            result.setSubMchid(isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                result.setSubAppid(isvsubMchParams.getSubMchAppId());
            }

        }else { // 普通商户

            result.setNormalMchid(wxPayService.getConfig().getMchId());
            result.setNormalAppid(wxPayService.getConfig().getAppId());

        }

        return result;
    }


}
