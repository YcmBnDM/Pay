package com.example.cache;

import com.alibaba.fastjson.JSON;
import com.example.util.SpringBeansUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class RedisUtil {
    private static StringRedisTemplate stringRedisTemplate = null;

    /**
     * 获取RedisTemplate对象, 默认使用 StringRedisTemplate, 客户端可查询
     **/
    private static final RedisTemplate getStringRedisTemplate() {

        if (stringRedisTemplate == null) {

            if (SpringBeansUtil.getApplicationContext().containsBean("defaultStringRedisTemplate")) {
                stringRedisTemplate = SpringBeansUtil.getBean("defaultStringRedisTemplate", StringRedisTemplate.class);
            } else {
                stringRedisTemplate = SpringBeansUtil.getBean(StringRedisTemplate.class);
            }
        }
        return stringRedisTemplate;
    }

    /**
     * 获取缓存数据, String类型
     */
    public static String getString(String key) {
        if (key == null) {
            return null;
        }
        return (String) getStringRedisTemplate().opsForValue().get(key);
    }

    /**
     * 获取缓存数据对象
     */
    public static <T> T getObject(String key, Class<T> cls) {

        String val = getString(key);
        return JSON.parseObject(val, cls);
    }

    /**
     * 放置缓存对象
     */
    public static void setString(String key, String value) {
        getStringRedisTemplate().opsForValue().set(key, value);
    }

    /**
     * 普通缓存放入并设置过期时间, 默认单位：秒
     */
    public static void setString(String key, String value, long time) {
        getStringRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 普通缓存放入并设置时间，自定义设置时间单位
     */
    public static void setString(String key, String value, long time, TimeUnit timeUnit) {
        getStringRedisTemplate().opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 放置缓存对象
     */
    public static void set(String key, Object value) {
        setString(key, JSON.toJSONString(value));
    }

    /**
     * 普通缓存放入并设置时间, 默认单位：秒
     */
    public static void set(String key, Object value, long time) {
        setString(key, JSON.toJSONString(value), time);
    }

    /**
     * 普通缓存放入并设置时间
     */
    public static void set(String key, Object value, long time, TimeUnit timeUnit) {
        setString(key, JSON.toJSONString(value), time, timeUnit);
    }

    /**
     * 指定缓存失效时间
     */
    public static void expire(String key, long time) {
        getStringRedisTemplate().expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 指定缓存失效时间
     */
    public static void expire(String key, long time, TimeUnit timeUnit) {
        getStringRedisTemplate().expire(key, time, timeUnit);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public static long getExpire(String key) {
        return getStringRedisTemplate().getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     */
    public static boolean hasKey(String key) {
        return getStringRedisTemplate().hasKey(key);
    }

    /**
     * 删除缓存
     **/
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                getStringRedisTemplate().delete(key[0]);
            } else {
                getStringRedisTemplate().delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 查询keys
     */
    public static Collection<String> keys(String pattern) {
        return getStringRedisTemplate().keys(pattern);
    }

}
