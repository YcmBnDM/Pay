package com.example.pay.rqrs.resp.payorder.payway.ysf;

import com.example.constants.CS;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import lombok.Data;

/**
 * 支付方式： YSF_BAR
 */
@Data
public class YsfBarOrderRS extends UnifiedOrderRS {

    @Override
    public String buildPayDataType() {
        return CS.PAY_DATA_TYPE.NONE;
    }

    @Override
    public String buildPayData() {
        return "";
    }

}
