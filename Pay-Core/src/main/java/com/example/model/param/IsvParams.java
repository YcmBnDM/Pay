
package com.example.model.param;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 抽象类 isv参数定义
 *
 */
public abstract class IsvParams {

    public static IsvParams factory(String ifCode, String paramsStr){

        try {
            return (IsvParams)JSONObject.parseObject(paramsStr, Class.forName(IsvParams.class.getPackage().getName() +"."+ ifCode +"."+ StrUtil.upperFirst(ifCode) +"IsvParams"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  敏感数据脱敏
    */
    public abstract String deSenData();

}
