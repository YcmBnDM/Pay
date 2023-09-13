package com.example.pay.channel.alipay;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.AbstractPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;

/**
 * 支付接口： 支付宝官方
 * 支付方式： 自适应
 */
@Service
public class AlipayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
    }

}
