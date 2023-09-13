package com.example.pay.rqrs.req.payorder;

import lombok.Data;

/**
 * 通用支付数据RQ
 */
@Data
public class CommonPayDataRQ extends UnifiedOrderRQ {

    /**
     * 请求参数： 支付数据包类型
     **/
    private String payDataType;

}
