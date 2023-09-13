package com.example.pay.rqrs.req.payorder.payway.cashier;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

/**
 * 支付方式： QR_CASHIER
 */
@Data
public class QrCashierOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public QrCashierOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.QR_CASHIER);
    }

}
