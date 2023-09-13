package com.example.pay.channel.plspay;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.AbstractPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;

/**
 * 计全支付plus
 *
 */
@Service
public class PlspayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PLSPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).preCheck(bizRQ, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(bizRQ, payOrder, mchAppConfigContext);
    }
}
