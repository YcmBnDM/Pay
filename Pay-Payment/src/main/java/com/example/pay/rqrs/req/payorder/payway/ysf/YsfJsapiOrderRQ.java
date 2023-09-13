package com.example.pay.rqrs.req.payorder.payway.ysf;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import lombok.Data;

/**
 * 支付方式： YSF_JSAPI
 */
@Data
public class YsfJsapiOrderRQ extends UnifiedOrderRQ {

    /**
     * 构造函数
     **/
    public YsfJsapiOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.YSF_JSAPI);
    }

}
