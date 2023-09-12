
package com.example.model.param;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 抽象类 特约商户参数定义
 */
public abstract class IsvsubMchParams {

    public static IsvsubMchParams factory(String ifCode, String paramsStr) {

        try {
            return (IsvsubMchParams) JSONObject.parseObject(paramsStr, Class.forName(IsvsubMchParams.class.getPackage().getName() + "." + ifCode + "." + StrUtil.upperFirst(ifCode) + "IsvsubMchParams"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
