
package com.example.pay.channel;


import com.example.entity.PayOrder;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;

/*
* 调起上游渠道侧支付接口
*
*/
public interface IPaymentService {

    /** 获取到接口code **/
    String getIfCode();

    /** 是否支持该支付方式 */
    boolean isSupport(String wayCode);

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder);

    /** 自定义支付订单号， 若返回空则使用系统生成订单号 */
    String customPayOrderId(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext);


    /** 调起支付接口，并响应数据；  内部处理普通商户和服务商模式  **/
    AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
