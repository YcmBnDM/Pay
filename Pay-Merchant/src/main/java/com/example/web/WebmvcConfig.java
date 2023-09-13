package com.example.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


/**
 * webmvc配置
 */
@Configuration
public class WebmvcConfig implements WebMvcConfigurer {

    @Resource
    private ApiResInterceptor apiResInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiResInterceptor);
    }
}
