package com.example.pay.model;

import com.example.entity.IsvInfo;
import com.example.model.param.IsvParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Isv支付参数信息 放置到内存， 避免多次查询操作
 */
@Data
public class IsvConfigContext {

    /**
     * isv信息缓存
     */
    private String isvNo;
    private IsvInfo isvInfo;

    /**
     * 商户支付配置信息缓存
     */
    private Map<String, IsvParams> isvParamsMap = new HashMap<>();


    /**
     * 缓存支付宝client 对象
     **/
    private AlipayClientWrapper alipayClientWrapper;

    /**
     * 缓存 wxServiceWrapper 对象
     **/
    private WxServiceWrapper wxServiceWrapper;


    /**
     * 获取isv配置信息
     **/
    public IsvParams getIsvParamsByIfCode(String ifCode) {
        return isvParamsMap.get(ifCode);
    }

    /**
     * 获取isv配置信息
     **/
    public <T> T getIsvParamsByIfCode(String ifCode, Class<? extends IsvParams> cls) {
        return (T) isvParamsMap.get(ifCode);
    }

}
