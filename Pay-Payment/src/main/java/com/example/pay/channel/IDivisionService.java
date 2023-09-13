
package com.example.pay.channel;


import com.example.entity.MchDivisionReceiver;
import com.example.entity.PayOrder;
import com.example.entity.PayOrderDivisionRecord;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;

import java.util.HashMap;
import java.util.List;

/**
* 分账接口
*
*/
public interface IDivisionService {

    /** 获取到接口code **/
    String getIfCode();

    /** 是否支持该分账 */
    boolean isSupport();

    /** 绑定关系 **/
    ChannelRetMsg bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext);

    /** 单次分账 （无需调用完结接口，或自动解冻商户资金)  **/
    ChannelRetMsg singleDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

    /** 查询分账结果  **/
    HashMap<Long, ChannelRetMsg> queryDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

}
