
package com.example.pay.channel.plspay.payway;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.model.param.plspay.PlspayConfig;
import com.example.pay.channel.plspay.PlspayKit;
import com.example.pay.channel.plspay.PlspayPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliWapOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import org.springframework.stereotype.Service;

/*
 * 计全付 支付宝 wap支付
 *
 */
@Service("plspayPaymentByAliWapService") //Service Name需保持全局唯一性
public class AliWap extends PlspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        // 构造函数响应数据
        AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            // 构建请求数据
            PayOrderCreateReqModel model = new PayOrderCreateReqModel();
            // 支付方式
            model.setWayCode(PlspayConfig.ALI_WAP);
            // 异步通知地址
            model.setNotifyUrl(getNotifyUrl());

            // 发起统一下单
            PayOrderCreateResponse response = PlspayKit.payRequest(payOrder, mchAppConfigContext, model);
            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            if (isSuccess) {
                // 下单成功
                String payUrl = response.getData().getString("payData");
                String payDataType = response.getData().getString("payDataType");
                if (CS.PAY_DATA_TYPE.FORM.equals(payDataType)) {
                    //表单方式
                    res.setFormContent(payUrl);
                } else if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(payDataType)) {
                    //二维码图片地址
                    res.setCodeImgUrl(payUrl);
                } else {
                    // 默认都为 payUrl方式
                    res.setPayUrl(payUrl);
                }
                channelRetMsg.setChannelOrderId(response.get().getPayOrderId());
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(response.getCode()+"");
                channelRetMsg.setChannelErrMsg(response.getMsg());
            }
        } catch (JeepayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }
        return res;
    }
}
