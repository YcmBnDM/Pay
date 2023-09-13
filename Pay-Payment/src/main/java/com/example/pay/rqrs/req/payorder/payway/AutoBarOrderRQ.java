package com.example.pay.rqrs.req.payorder.payway;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import lombok.Data;

/**
 * 支付方式： AUTO_BAR
 */
@Data
public class AutoBarOrderRQ extends UnifiedOrderRQ {

    /**
     * 条码值
     **/
    private String authCode;

    /**
     * 构造函数
     **/
    public AutoBarOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.AUTO_BAR);
    }

}
