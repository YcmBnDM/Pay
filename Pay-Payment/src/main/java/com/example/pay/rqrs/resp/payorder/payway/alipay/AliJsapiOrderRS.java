package com.example.pay.rqrs.resp.payorder.payway.alipay;

import com.example.constants.CS;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import com.example.util.JsonUtil;
import lombok.Data;

/**
 * 支付方式： ALI_JSAPI
 */
@Data
public class AliJsapiOrderRS extends UnifiedOrderRS {

    /**
     * 调起支付插件的支付宝订单号
     **/
    private String alipayTradeNo;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData() {
        return JsonUtil.newJson("alipayTradeNo", alipayTradeNo).toString();
    }

}
