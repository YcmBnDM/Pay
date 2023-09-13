package com.example.pay.rqrs.req.payorder.payway.union;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

/**
 * 支付方式： UPACP_APP
 */
@Data
public class UpAppOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public UpAppOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_APP);
    }

}
