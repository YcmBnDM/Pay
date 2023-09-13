package com.example.pay.channel.alipay;

import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.IPayOrderCloseService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ConfigContextQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 支付宝 关闭订单接口实现类
 */
@Service
public class AlipayPayOrderCloseService implements IPayOrderCloseService {

    @Resource
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AlipayTradeCloseRequest req = new AlipayTradeCloseRequest();

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeCloseResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 返回状态成功
        if (resp.isSuccess()) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());
        } else {
            return ChannelRetMsg.sysError(resp.getSubMsg());
        }
    }


}
