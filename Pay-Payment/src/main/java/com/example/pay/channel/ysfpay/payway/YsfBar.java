
package com.example.pay.channel.ysfpay.payway;

import com.alibaba.fastjson.JSONObject;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.pay.channel.ysfpay.YsfpayPaymentService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractRS;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.ysf.YsfBarOrderRQ;
import com.example.pay.rqrs.resp.payorder.payway.ysf.YsfBarOrderRS;
import com.example.pay.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 云闪付 云闪付条码支付
 *
 */
@Service("ysfPaymentByYsfBarService") //Service Name需保持全局唯一性
public class YsfBar extends YsfpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        YsfBarOrderRQ bizRQ = (YsfBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【云闪付条码(unionpay)支付】";

        YsfBarOrderRQ bizRQ = (YsfBarOrderRQ) rq;
        YsfBarOrderRS res = ApiResBuilder.buildSuccess(YsfBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        JSONObject reqParams = new JSONObject();
        reqParams.put("authCode", bizRQ.getAuthCode().trim()); //付款码： 用户 APP 展示的付款条码或二维码

        // 云闪付 bar 统一参数赋值
        barParamsSet(reqParams, payOrder);

        //客户端IP
        reqParams.put("termInfo", "{\"ip\": \""+StringUtils.defaultIfEmpty(payOrder.getClientIp(), "127.0.0.1")+"\"}"); //终端信息

        // 发送请求
        JSONObject resJSON = packageParamAndReq("/gateway/api/pay/micropay", reqParams, logPrefix, mchAppConfigContext);
        //请求 & 响应成功， 判断业务逻辑
        String respCode = resJSON.getString("respCode"); //应答码
        String respMsg = resJSON.getString("respMsg"); //应答信息

        try {
            //00-交易成功， 02-用户支付中 , 12-交易重复， 需要发起查询处理    其他认为失败
            if("00".equals(respCode)){
                res.setPayData(resJSON.getString("payData"));
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }else if("02".equals(respCode) ||"12".equals(respCode) || "99".equals(respCode)){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                channelRetMsg.setNeedQuery(true); // 开启轮询查单
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelErrCode(respCode);
            channelRetMsg.setChannelErrMsg(respMsg);
        }
        return res;
    }

}
