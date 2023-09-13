package com.example.pay.rqrs.resp.payorder.payway.ysf;

import com.example.constants.CS;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import com.example.util.JsonUtil;
import lombok.Data;

/**
 * 支付方式： YSF_JSAPI
 *
 */
@Data
public class YsfJsapiOrderRS extends UnifiedOrderRS {

    /**
     * 调起支付插件的云闪付订单号
     **/
    private String redirectUrl;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.YSF_APP;
    }

    @Override
    public String buildPayData() {
        return JsonUtil.newJson("redirectUrl", redirectUrl).toString();
    }

}
