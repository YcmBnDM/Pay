package com.example.pay.rqrs.req.payorder.payway.alipay;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

/**
 * 支付方式： ALI_QR
 */
@Data
public class AliQrOrderRQ extends CommonPayDataRQ {

    /**
     * 构造函数
     **/
    public AliQrOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_QR);
    }

}
