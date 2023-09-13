package com.example.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据响应拦截器
 */
@Component
public class ApiResInterceptor implements HandlerInterceptor {

    /**
     * postHandler是在请求结束之后, 视图渲染之前执行的,但只有preHandle方法返回true的时候才会执行
     * 如果ctrl使用了@RestController或者@ResponseBody注解 则ModelAndView = null, 因为不走视图转换器, 而是走的RequestResponseBodyMethodProcessor。
     * ————————————————
     **/
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        //do
    }

}
