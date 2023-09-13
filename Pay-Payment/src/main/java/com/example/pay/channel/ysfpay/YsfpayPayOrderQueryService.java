
package com.example.pay.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.pay.channel.IPayOrderQueryService;
import com.example.pay.channel.ysfpay.utils.YsfHttpUtil;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

;

/**
 * 云闪付查单
 *
 */
@Service
@Slf4j
public class YsfpayPayOrderQueryService implements IPayOrderQueryService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Resource
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")查单】";

        try {
            reqParams.put("orderNo", payOrder.getPayOrderId()); //订单号
            reqParams.put("orderType", orderType); //订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/queryOrder", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 payorderId:{}, 返回结果:{}", payOrder.getPayOrderId(), resJSON);
            if(resJSON == null){
                return ChannelRetMsg.waiting(); //支付中
            }

            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String origRespCode = resJSON.getString("origRespCode"); //原交易应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            if(("00").equals(respCode)){//如果查询交易成功
                //00- 支付成功 01- 转入退款 02- 未支付 03- 已关闭 04- 已撤销(付款码支付) 05- 用户支付中 06- 支付失败
                if(("00").equals(origRespCode)){

                    //交易成功，更新商户订单状态
                    return ChannelRetMsg.confirmSuccess(resJSON.getString("transIndex"));  //支付成功

                }else if( "02".equals(origRespCode) || "05".equals(origRespCode) ) {

                    return ChannelRetMsg.waiting(); //支付中
                }
            }
            return ChannelRetMsg.waiting(); //支付中
        }catch (Exception e) {
            return ChannelRetMsg.waiting(); //支付中
        }
    }

}
