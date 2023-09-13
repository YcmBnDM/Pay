package com.example.pay.channel.pppay;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.IPayOrderQueryService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ConfigContextQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

;

/**
 * none.
 *
 */
@Service
public class PppayPayOrderQueryService implements IPayOrderQueryService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PPPAY;
    }

    @Resource
    private ConfigContextQueryService configContextQueryService;

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return configContextQueryService.getPaypalWrapper(mchAppConfigContext).processOrder(null, payOrder);
    }
}
