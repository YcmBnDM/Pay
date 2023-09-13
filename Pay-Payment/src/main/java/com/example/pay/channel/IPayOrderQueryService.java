
package com.example.pay.channel;

import com.example.entity.PayOrder;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;

/**
 * 查单（渠道侧）接口定义
 **/
public interface IPayOrderQueryService {

    /**
     * 获取到接口code
     **/
    String getIfCode();

    /**
     * 查询订单
     **/
    ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
