package com.example.util;

import com.alibaba.fastjson.JSONObject;

/**
 * Json工具类
 */
public class JsonUtil {
    public static JSONObject newJson(String key, Object val){

        JSONObject result = new JSONObject();
        result.put(key, val);
        return result;
    }
}
