package com.example.jwt;

import com.alibaba.fastjson.JSONObject;
import com.example.model.security.PayUserDetails;
import lombok.Data;

import java.util.Map;


/**
 *
 * JWT payload 载体
 * 格式：
 *     {
 *         "sysUserId": "10001",
 *         "created": "1568250147846",
 *         "cacheKey": "KEYKEYKEYKEY",
 *     }
 *
 */
@Data
public class JWTPayload {
    // 登录用户ID
    private Long sysUserId;

    // 创建时间
    private Long created;

    // Redis 保存的Key
    private String cacheKey;


    protected JWTPayload(){}

    public JWTPayload(PayUserDetails payUserDetails){

        this.setSysUserId(payUserDetails.getSysUser().getSysUserId());
        this.setCreated(System.currentTimeMillis());
        this.setCacheKey(payUserDetails.getCacheKey());
    }


    /** toMap **/
    public Map<String, Object> toMap(){
        JSONObject json = (JSONObject)JSONObject.toJSON(this);
        return json.toJavaObject(Map.class);
    }
}
