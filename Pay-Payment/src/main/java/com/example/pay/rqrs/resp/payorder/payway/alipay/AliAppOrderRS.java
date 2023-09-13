package com.example.pay.rqrs.resp.payorder.payway.alipay;

import com.example.constants.CS;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import lombok.Data;

/**
 * 支付方式： ALI_APP
 */
@Data
public class AliAppOrderRS extends UnifiedOrderRS {

    private String payData;

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.ALI_APP;
    }

    @Override
    public String buildPayData() {
        return payData;
    }

}
