
package com.example.pay.channel.xxpay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.entity.RefundOrder;
import com.example.model.param.xxpay.XxpayNormalMchParams;
import com.example.pay.channel.AbstractRefundService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.rqrs.req.refund.RefundOrderRQ;
import com.example.util.PayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/*
* 退款接口： 小新支付
*
*/
@Service
@Slf4j
public class XxpayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        XxpayNormalMchParams params = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

        // 构造支付请求参数
        Map<String,Object> paramMap = new TreeMap();
        paramMap.put("mchId", params.getMchId());  //商户ID
        paramMap.put("mchOrderNo", refundOrder.getPayOrderId());   //支付订单-商户订单号
        paramMap.put("mchRefundNo", refundOrder.getRefundOrderId());   //商户退款单号
        paramMap.put("amount", refundOrder.getRefundAmount());   //退款金额
        paramMap.put("currency", "cny");   //币种
        paramMap.put("clientIp", refundOrder.getClientIp());   //客户端IP
        paramMap.put("device", "web");   //客户端设备
        //如果notifyUrl 不为空表示异步退款，具体退款结果以退款通知为准
        paramMap.put("notifyUrl", getNotifyUrl(refundOrder.getRefundOrderId()));   // 异步退款通知
        paramMap.put("remarkInfo", refundOrder.getRefundReason());   // 退款原因

        // 生成签名
        String sign = XxpayKit.getSign(paramMap, params.getKey());
        paramMap.put("sign", sign);
        // 退款地址
        String refundUrl = XxpayKit.getRefundUrl(params.getPayUrl())+ "?" + PayUtil.genUrlParams(paramMap);
        String resStr = "";
        try {
            log.info("发起退款[{}]参数：{}", getIfCode(), refundUrl);
            resStr = HttpUtil.createPost(refundUrl).timeout(60 * 1000).execute().body();
            log.info("发起退款[{}]结果：{}", getIfCode(), resStr);
        } catch (Exception e) {
            log.error("http error", e);
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        // 默认退款中状态
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        if(StringUtils.isEmpty(resStr)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg("请求"+getIfCode()+"接口异常");
            return null;
        }

        JSONObject resObj = JSONObject.parseObject(resStr);
        if(!"0".equals(resObj.getString("retCode"))){
            String retMsg = resObj.getString("retMsg");
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg(retMsg);
            return null;
        }

        // 验证响应数据签名
        String checkSign = resObj.getString("sign");
        resObj.remove("sign");
        if(!checkSign.equals(XxpayKit.getSign(resObj, params.getKey()))) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            return null;
        }

        // 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        String status = resObj.getString("status");
        if("2".equals(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else if("3".equals(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrMsg(resObj.getString("retMsg"));
        }

        return channelRetMsg;

    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        XxpayNormalMchParams params = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

        // 构造支付请求参数
        Map<String,Object> paramMap = new TreeMap();
        paramMap.put("mchId", params.getMchId());  //商户ID
        paramMap.put("mchRefundNo", refundOrder.getRefundOrderId());   //商户退款单号

        // 生成签名
        String sign = XxpayKit.getSign(paramMap, params.getKey());
        paramMap.put("sign", sign);
        // 退款查询地址
        String queryRefundOrderUrl = XxpayKit.getQueryRefundOrderUrl(params.getPayUrl())+ "?" + PayUtil.genUrlParams(paramMap);
        String resStr = "";
        try {
            log.info("查询退款[{}]参数：{}", getIfCode(), queryRefundOrderUrl);
            resStr = HttpUtil.createPost(queryRefundOrderUrl).timeout(60 * 1000).execute().body();
            log.info("查询退款[{}]结果：{}", getIfCode(), resStr);
        } catch (Exception e) {
            log.error("http error", e);
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        // 默认退款中状态
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        if(StringUtils.isEmpty(resStr)) {
            return null;
        }

        JSONObject resObj = JSONObject.parseObject(resStr);
        if(!"0".equals(resObj.getString("retCode"))){
            return null;
        }

        // 验证响应数据签名
        String checkSign = resObj.getString("sign");
        resObj.remove("sign");
        if(!checkSign.equals(XxpayKit.getSign(resObj, params.getKey()))) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            return null;
        }

        // 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        String status = resObj.getString("status");
        if("2".equals(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else if("3".equals(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrMsg(resObj.getString("retMsg"));
        }

        return channelRetMsg;

    }

}
