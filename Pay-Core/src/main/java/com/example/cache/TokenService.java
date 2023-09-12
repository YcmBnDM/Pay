package com.example.cache;

import com.example.constants.CS;
import com.example.model.security.PayUserDetails;

/**
 * Token Service
 */
public class TokenService {

    /**
     * 处理Token信息
     * 1. 如果不允许多用户则踢掉之前的所有用户信息
     * 2. 更新token 缓存时间信息
     * 3. 更新用户token列表
     * @param payUserDetails
     * @param cacheKey
     */
    public static void processTokenCache(PayUserDetails payUserDetails, String cacheKey) {
        payUserDetails.setCacheKey(cacheKey);
        // 缓存时间2小时, 保存具体信息而只是uid, 因为很多场景需要得到信息，
        // 例如验证接口权限， 每次请求都需要获取。 将信息封装在一起减少磁盘请求次数， 如果放置多个key会增加非顺序读取。
        RedisUtil.set(cacheKey, payUserDetails, CS.TOKEN_TIME);
    }


    /**
     * 删除Token
     * @param iToken
     */
    public static void removeToken(String iToken) {
        RedisUtil.del(iToken);
    }


    /**
     * 刷新Token
     * @param payUserDetails
     */
    public static void refData(PayUserDetails payUserDetails) {
        RedisUtil.set(payUserDetails.getCacheKey(), payUserDetails, CS.TOKEN_TIME);
    }
}
