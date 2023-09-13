package com.example.pay.rqrs.resp.payorder;

import com.alibaba.fastjson.annotation.JSONField;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/**
 * 关闭订单 响应参数
 */
@Data
public class ClosePayOrderRS extends AbstractRS {

    /**
     * 上游渠道返回数据包 (无需JSON序列化)
     **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;

}
