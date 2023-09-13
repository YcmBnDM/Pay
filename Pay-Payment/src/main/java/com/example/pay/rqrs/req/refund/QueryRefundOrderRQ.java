package com.example.pay.rqrs.req.refund;

import com.example.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;

/**
 * 查询退款单请求参数对象
 */
@Data
public class QueryRefundOrderRQ extends AbstractMchAppRQ {

    /**
     * 商户退款单号
     **/
    private String mchRefundNo;

    /**
     * 支付系统退款订单号
     **/
    private String refundOrderId;

}
