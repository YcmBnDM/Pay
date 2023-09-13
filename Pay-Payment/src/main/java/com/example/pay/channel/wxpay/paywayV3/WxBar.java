package com.example.pay.channel.wxpay.paywayV3;


import com.example.entity.PayOrder;
import com.example.pay.channel.wxpay.WxpayPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

;

/*
 * 微信 条码支付
 */
@Service("wxpayPaymentByBarV3Service") //Service Name需保持全局唯一性
public class WxBar extends WxpayPaymentService {

    @Resource
    private WxBar wxBar;

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return wxBar.preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return wxBar.pay(rq, payOrder, mchAppConfigContext);
    }
}
