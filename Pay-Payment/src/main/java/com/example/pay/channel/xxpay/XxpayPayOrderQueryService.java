
package com.example.pay.channel.xxpay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.model.param.xxpay.XxpayNormalMchParams;
import com.example.pay.channel.IPayOrderQueryService;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.ChannelRetMsg;
import com.example.pay.service.ConfigContextQueryService;
import com.example.util.PayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.TreeMap;

;

/*
* 小新支付 查单接口实现类
*
*/
@Service
@Slf4j
public class XxpayPayOrderQueryService implements IPayOrderQueryService {

    @Resource
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext){
        XxpayNormalMchParams xxpayParams = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
        Map<String,Object> paramMap = new TreeMap();
        // 接口类型
        paramMap.put("mchId", xxpayParams.getMchId());
        paramMap.put("mchOrderNo", payOrder.getPayOrderId());
        String sign = XxpayKit.getSign(paramMap, xxpayParams.getKey());
        paramMap.put("sign", sign);
        String resStr = "";
        String queryPayOrderUrl = XxpayKit.getQueryPayOrderUrl(xxpayParams.getPayUrl()) + "?" + PayUtil.genUrlParams(paramMap);
        try {
            log.info("支付查询[{}]参数：{}", getIfCode(), queryPayOrderUrl);
            resStr = HttpUtil.createPost(queryPayOrderUrl).timeout(60 * 1000).execute().body();
            log.info("支付查询[{}]结果：{}", getIfCode(), resStr);
        } catch (Exception e) {
            log.error("http error", e);
        }
        if(StringUtils.isEmpty(resStr)) {
            return ChannelRetMsg.waiting(); //支付中
        }
        JSONObject resObj = JSONObject.parseObject(resStr);
        if(!"0".equals(resObj.getString("retCode"))){
            return ChannelRetMsg.waiting(); //支付中
        }
        // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
        String status = resObj.getString("status");
        if("2".equals(status) || "3".equals(status)) {
            return ChannelRetMsg.confirmSuccess(resObj.getString("channelOrderNo"));  //支付成功
        }
        return ChannelRetMsg.waiting(); //支付中
    }


}
