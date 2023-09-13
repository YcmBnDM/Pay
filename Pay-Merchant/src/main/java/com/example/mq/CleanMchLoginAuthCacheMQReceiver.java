package com.example.mq;

import com.example.cache.RedisUtil;
import com.example.components.mq.model.extender.CleanMchLoginAuthCacheMQ;
import com.example.constants.CS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 接收MQ消息
 * 业务： 清除商户登录信息
 */
@Slf4j
@Component
public class CleanMchLoginAuthCacheMQReceiver implements CleanMchLoginAuthCacheMQ.IMQReceiver {

    @Override
    public void receive(CleanMchLoginAuthCacheMQ.MsgPayload payload) {

        log.info("成功接收删除商户用户登录的订阅通知, msg={}", payload);
        // 字符串转List<Long>
        List<Long> userIdList = payload.getUserIdList();
        // 删除redis用户缓存
        if (userIdList == null || userIdList.isEmpty()) {
            log.info("用户ID为空");
            return;
        }
        for (Long sysUserId : userIdList) {
            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if (cacheKeyList == null || cacheKeyList.isEmpty()) {
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                // 删除用户Redis信息
                RedisUtil.del(cacheKey);
                continue;
            }
        }
        log.info("无权限登录用户信息已清除");
    }
}
