package com.example.pay.channel.alipay;

import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
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
 * 支付宝 查单接口实现类
 */
@Service
public class AlipayPayOrderQueryService implements IPayOrderQueryService {

    @Resource
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeQueryResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);
        String result = resp.getTradeStatus();

        if ("TRADE_SUCCESS".equals(result)) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());  //支付成功
        } else if ("WAIT_BUYER_PAY".equals(result)) {
            return ChannelRetMsg.waiting(); //支付中
        }
        return ChannelRetMsg.waiting(); //支付中
    }


}
