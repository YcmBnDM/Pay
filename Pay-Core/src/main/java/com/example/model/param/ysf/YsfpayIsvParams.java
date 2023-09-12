
package com.example.model.param.ysf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.model.param.IsvParams;
import com.example.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 云闪付 配置信息
 */
@Data
public class YsfpayIsvParams extends IsvParams {

    /**
     * 是否沙箱环境
     */
    private Byte sandbox;

    /**
     * serProvId
     **/
    private String serProvId;

    /**
     * isvPrivateCertFile 证书
     **/
    private String isvPrivateCertFile;

    /**
     * isvPrivateCertPwd
     **/
    private String isvPrivateCertPwd;

    /**
     * ysfpayPublicKey
     **/
    private String ysfpayPublicKey;

    /**
     * acqOrgCodeList 支付机构号
     **/
    private String acqOrgCode;

    @Override
    public String deSenData() {

        YsfpayIsvParams isvParams = this;
        if (StringUtils.isNotBlank(this.isvPrivateCertPwd)) {
            isvParams.setIsvPrivateCertPwd(StringUtil.str2Star(this.isvPrivateCertPwd, 0, 3, 6));
        }
        if (StringUtils.isNotBlank(this.ysfpayPublicKey)) {
            isvParams.setYsfpayPublicKey(StringUtil.str2Star(this.ysfpayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }

}
