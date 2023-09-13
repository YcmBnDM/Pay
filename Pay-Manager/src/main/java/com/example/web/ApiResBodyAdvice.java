package com.example.web;

import com.example.util.other.ApiResBodyAdviceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 功能： 自定义springMVC返回数据格式
 */
@ControllerAdvice
public class ApiResBodyAdvice implements ResponseBodyAdvice {

    /**
     * 注入 是否开启 knife4j
     **/
    @Value("${knife4j.enable}")
    private boolean knife4jEnable = false;

    /**
     * 判断哪些需要拦截
     **/
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {

        // springfox.documentation.swagger.web.ApiResourceController    -- /swagger-resources
        // springfox.documentation.swagger2.web.Swagger2ControllerWebMvc  -- /v2/api-docs
        if (knife4jEnable && returnType.getMethod().getDeclaringClass().getName().startsWith("springfox.documentation.swagger")) {
            return false;
        }

        return true;
    }

    /**
     * 拦截返回数据处理
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        //处理扩展字段
        return ApiResBodyAdviceUtil.beforeBodyWrite(body);
    }

}
