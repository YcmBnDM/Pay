package com.example.pay.rqrs.req.payorder.payway.union;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

/**
 * 支付方式： UPACP_PC
 */
@Data
public class UpPcOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public UpPcOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_PC);
    }

}
