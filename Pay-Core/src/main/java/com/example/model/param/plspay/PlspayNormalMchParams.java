
package com.example.model.param.plspay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.model.param.NormalMchParams;
import com.example.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * JeePay - plus， 普通商户参数定义
 */
@Data
public class PlspayNormalMchParams extends NormalMchParams {

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 签名方式
     **/
    private String signType;

    /**
     * md5秘钥
     */
    private String appSecret;

    /**
     * RSA2: 应用私钥
     */
    private String rsa2AppPrivateKey;

    /**
     * RSA2: 支付网关公钥
     */
    public String rsa2PayPublicKey;


    @Override
    public String deSenData() {

        PlspayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            mchParams.setAppSecret(StringUtil.str2Star(this.appSecret, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
