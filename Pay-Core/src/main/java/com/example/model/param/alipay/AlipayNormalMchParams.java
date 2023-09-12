
package com.example.model.param.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.model.param.NormalMchParams;
import com.example.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 支付宝 普通商户参数定义
 */
@Data
public class AlipayNormalMchParams extends NormalMchParams {

    /**
     * 是否沙箱环境
     */
    private Byte sandbox;

    /**
     * appId
     */
    private String appId;

    /**
     * privateKey
     */
    private String privateKey;

    /**
     * alipayPublicKey
     */
    private String alipayPublicKey;

    /**
     * 签名方式
     **/
    private String signType;

    /**
     * 是否使用证书方式
     **/
    private Byte useCert;

    /**
     * app 证书
     **/
    private String appPublicCert;

    /**
     * 支付宝公钥证书（.crt格式）
     **/
    private String alipayPublicCert;

    /**
     * 支付宝根证书
     **/
    private String alipayRootCert;

    @Override
    public String deSenData() {

        AlipayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.privateKey)) {
            mchParams.setPrivateKey(StringUtil.str2Star(this.privateKey, 4, 4, 6));
        }
        if (StringUtils.isNotBlank(this.alipayPublicKey)) {
            mchParams.setAlipayPublicKey(StringUtil.str2Star(this.alipayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
