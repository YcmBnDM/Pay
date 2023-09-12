package com.example.service;

import com.example.model.DBApplicationConfig;

public interface ISysConfigService {

    /** 获取应用的配置参数 **/
    DBApplicationConfig getDBApplicationConfig();

}