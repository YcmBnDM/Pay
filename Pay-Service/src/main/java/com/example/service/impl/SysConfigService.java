package com.example.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.SysConfig;
import com.example.model.DBApplicationConfig;
import com.example.service.ISysConfigService;
import com.example.service.mapper.SysConfigMapper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * 系统配置表 服务实现类
 */
@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    /**
     * 是否启用缓存
     * true: 表示将使用内存缓存， 将部分系统配置项 或 商户应用/服务商信息进行缓存并读取
     * false: 直接查询DB
     **/
    public static boolean IS_USE_CACHE = false;

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 数据库application配置参数
     **/
    private static MutablePair<String, DBApplicationConfig> APPLICATION_CONFIG = new MutablePair<>("applicationConfig", null);

    public synchronized void initDBConfig(String groupKey) {

        // 若当前系统不缓存，则直接返回
        if (!IS_USE_CACHE) {
            return;
        }

        if (APPLICATION_CONFIG.getLeft().equalsIgnoreCase(groupKey)) {
            APPLICATION_CONFIG.right = this.selectByGroupKey(groupKey).toJavaObject(DBApplicationConfig.class);
        }
    }

    /**
     * 获取实际的数据
     **/
    @Override
    public DBApplicationConfig getDBApplicationConfig() {

        // 查询DB
        if (!IS_USE_CACHE) {
            return this.selectByGroupKey(APPLICATION_CONFIG.getLeft()).toJavaObject(DBApplicationConfig.class);
        }

        // 缓存数据
        if (APPLICATION_CONFIG.getRight() == null) {
            initDBConfig(APPLICATION_CONFIG.getLeft());
        }
        return APPLICATION_CONFIG.right;
    }


    /**
     * 根据分组查询，并返回JSON对象格式的数据
     **/
    public JSONObject selectByGroupKey(String groupKey) {

        JSONObject result = new JSONObject();
        list(SysConfig.gw().select(SysConfig::getConfigKey, SysConfig::getConfigVal).eq(SysConfig::getGroupKey, groupKey)).stream().forEach(item -> result.put(item.getConfigKey(), item.getConfigVal()));
        return result;
    }


    public int updateByConfigKey(Map<String, String> updateMap) {
        int count = 0;
        Set<String> set = updateMap.keySet();
        for (String k : set) {
            SysConfig sysConfig = new SysConfig();
            sysConfig.setConfigKey(k);
            sysConfig.setConfigVal(updateMap.get(k));
            boolean update = sysConfigService.saveOrUpdate(sysConfig);
            if (update) {
                count++;
            }
        }
        return count;
    }
}
