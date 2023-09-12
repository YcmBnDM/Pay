
package com.example.model.param.plspay;

import lombok.Data;

/**
 * JeePay-Plus， 通用配置信息
 */
@Data
public class PlspayConfig {

    /**
     * 签名类型
     */
    public static final String DEFAULT_SIGN_TYPE = "MD5";
    public static final String SIGN_TYPE_RSA2 = "RSA2";

    /**
     * 支付订单状态
     */
    public static String PAY_STATE_SUCCESS = "2";    // 2-支付成功
    public static String PAY_STATE_FAIL = "3";       // 3-支付失败

    /**
     * 退款订单状态
     */
    public static String REFUND_STATE_SUCCESS = "2";    // 2-退款成功
    public static String REFUND_STATE_FAIL = "3";       // 3-退款失败

    /**
     * 支付方式
     */
    public static String ALI_BAR = "ALI_BAR";               // 支付宝条码
    public static String ALI_JSAPI = "ALI_JSAPI";           // 支付宝生活号
    public static String ALI_LITE = "ALI_LITE";             // 支付宝小程序
    public static String ALI_APP = "ALI_APP";               // 支付宝APP
    public static String ALI_WAP = "ALI_WAP";               // 支付宝WAP
    public static String ALI_PC = "ALI_PC";                 // 支付宝PC网站
    public static String ALI_QR = "ALI_QR";                 // 支付宝二维码
    public static String WX_BAR = "WX_BAR";                 // 微信条码
    public static String WX_JSAPI = "WX_JSAPI";             // 微信公众号
    public static String WX_LITE = "WX_LITE";               // 微信小程序
    public static String WX_APP = "WX_APP";                 // 微信APP
    public static String WX_H5 = "WX_H5";                   // 微信H5
    public static String WX_NATIVE = "WX_NATIVE";           // 微信扫码
}