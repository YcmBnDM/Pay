
package com.example.pay.channel;

import com.example.entity.PayOrder;
import com.example.entity.RefundOrder;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.refund.RefundOrderRQ;

/*
* 调起上游渠道侧退款接口
*
*/
public interface IRefundService {

    /** 获取到接口code **/
    String getIfCode();

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder);

    /** 调起退款接口，并响应数据；  内部处理普通商户和服务商模式  **/
    ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

    /** 退款查单接口  **/
    ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
