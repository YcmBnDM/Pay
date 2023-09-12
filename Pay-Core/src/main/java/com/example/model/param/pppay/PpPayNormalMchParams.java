package com.example.model.param.pppay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.model.param.NormalMchParams;
import com.example.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
public class PpPayNormalMchParams extends NormalMchParams {
    /**
     * 是否沙箱环境
     */
    private Byte sandbox;

    /**
     * clientId
     * 客户端 ID
     */
    private String clientId;

    /**
     * secret
     * 密钥
     */
    private String secret;

    /**
     * 支付 Webhook 通知 ID
     */
    private String notifyWebhook;

    /**
     * 退款 Webhook 通知 ID
     */
    private String refundWebhook;

    @Override
    public String deSenData() {
        PpPayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.secret)) {
            mchParams.setSecret(StringUtil.str2Star(this.secret, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }
}
