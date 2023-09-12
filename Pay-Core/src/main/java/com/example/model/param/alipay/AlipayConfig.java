
package com.example.model.param.alipay;

import lombok.Data;

/**
 * 支付宝， 通用配置信息
 */
@Data
public class AlipayConfig {

    public static final String SIGN_TYPE_RSA = "RSA";
    public static final String SIGN_TYPE_RSA2 = "RSA2";


    /**
     * 网关地址
     */
    public static String PROD_SERVER_URL = "https://openapi.alipay.com/gateway.do";
    public static String SANDBOX_SERVER_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    public static String PROD_OAUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";
    public static String SANDBOX_OAUTH_URL = "https://openapi-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";

    /**
     * isv获取授权商户URL地址
     */
    public static String PROD_APP_TO_APP_AUTH_URL = "https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";
    public static String SANDBOX_APP_TO_APP_AUTH_URL = "https://openapi-sandbox.dl.alipaydev.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";


    public static String FORMAT = "json";

    public static String CHARSET = "UTF-8";

}
