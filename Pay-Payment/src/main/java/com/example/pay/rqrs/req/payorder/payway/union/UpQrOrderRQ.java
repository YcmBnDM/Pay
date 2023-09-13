package com.example.pay.rqrs.req.payorder.payway.union;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

/**
 * 支付方式： UPACP_QR
 */
@Data
public class UpQrOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public UpQrOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_QR);
    }

}
