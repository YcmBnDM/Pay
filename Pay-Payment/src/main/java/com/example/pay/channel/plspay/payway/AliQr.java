
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
import com.example.pay.rqrs.req.payorder.payway.alipay.AliQrOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.alipay.AliQrOrderRS;
import com.example.pay.util.ApiResBuilder;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import org.springframework.stereotype.Service;

/*
 * 计全付 支付宝 二维码支付
 *

 */
@Service("plspayPaymentByAliQrService") //Service Name需保持全局唯一性
public class AliQr extends PlspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        AliQrOrderRQ aliQrOrderRQ = (AliQrOrderRQ) rq;
        // 构造函数响应数据
        AliQrOrderRS res = ApiResBuilder.buildSuccess(AliQrOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            // 构建请求数据
            PayOrderCreateReqModel model = new PayOrderCreateReqModel();
            // 支付方式
            model.setWayCode(PlspayConfig.ALI_QR);
            // 异步通知地址
            model.setNotifyUrl(getNotifyUrl());

            // 发起统一下单
            PayOrderCreateResponse response = PlspayKit.payRequest(payOrder, mchAppConfigContext, model);
            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            if (isSuccess) {
                // 下单成功
                // 二维码图片地址
                if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(aliQrOrderRQ.getPayDataType())) {
                    res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(response.get().getPayData()));
                } else {
                    res.setCodeUrl(response.get().getPayData());
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