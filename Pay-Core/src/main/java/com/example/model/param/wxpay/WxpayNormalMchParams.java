
package com.example.model.param.wxpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.model.param.NormalMchParams;
import com.example.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 微信官方支付 配置参数
 */
@Data
public class WxpayNormalMchParams extends NormalMchParams {

    /**
     * 应用App ID
     */
    private String appId;

    /**
     * 应用AppSecret
     */
    private String appSecret;

    /**
     * 微信支付商户号
     */
    private String mchId;

    /**
     * oauth2地址
     */
    private String oauth2Url;

    /**
     * API密钥
     */
    private String key;

    /**
     * 微信支付API版本
     **/
    private String apiVersion;

    /**
     * API V3秘钥
     **/
    private String apiV3Key;

    /**
     * 序列号
     **/
    private String serialNo;

    /**
     * API证书(.p12格式)
     **/
    private String cert;

    /**
     * 证书文件(.pem格式)
     **/
    private String apiClientCert;

    /**
     * 私钥文件(.pem格式)
     **/
    private String apiClientKey;

    @Override
    public String deSenData() {
        WxpayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            mchParams.setAppSecret(StringUtil.str2Star(this.appSecret, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.key)) {
            mchParams.setKey(StringUtil.str2Star(this.key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.apiV3Key)) {
            mchParams.setApiV3Key(StringUtil.str2Star(this.apiV3Key, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.serialNo)) {
            mchParams.setSerialNo(StringUtil.str2Star(this.serialNo, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
