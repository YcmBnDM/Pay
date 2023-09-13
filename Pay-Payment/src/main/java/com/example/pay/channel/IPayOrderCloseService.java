
package com.example.pay.channel;


import com.example.entity.PayOrder;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;

/**
 * 关闭订单（渠道侧）接口定义
 *
 */
public interface IPayOrderCloseService {

    /** 获取到接口code **/
    String getIfCode();

    /** 查询订单 **/
    ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
