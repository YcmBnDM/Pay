package com.example.pay.rqrs.resp.payorder.payway.weixin;

import com.example.constants.CS;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import lombok.Data;

/**
 * 支付方式： WX_APP
 */
@Data
public class WxAppOrderRS extends UnifiedOrderRS {

    /**
     * 预支付数据包
     **/
    private String payInfo;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.WX_APP;
    }

    @Override
    public String buildPayData() {
        return payInfo;
    }

}
