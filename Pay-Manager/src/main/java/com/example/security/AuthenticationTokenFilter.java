package com.example.security;


import com.example.cache.RedisUtil;
import com.example.config.SystemYmlConfig;
import com.example.constants.CS;
import com.example.jwt.JWTPayload;
import com.example.jwt.JWTUtil;
import com.example.model.security.PayUserDetails;
import com.example.util.SpringBeansUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p><b>Title: </b>JeeAuthenticationTokenFilter.java
 * <p><b>Description: </b>
 * spring security框架中验证组件的前置过滤器；
 * 用于验证token有效期，并放置ContextAuthentication信息,为后续spring security框架验证提供数据；
 * 避免使用@Component等bean自动装配注解：@Component会将filter被spring实例化为web容器的全局filter，导致重复过滤。
 */
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        PayUserDetails jeeUserDetails = commonFilter(request);

        if (jeeUserDetails == null) {
            chain.doFilter(request, response);
            return;
        }

        //将信息放置到Spring-security context中
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jeeUserDetails, null, jeeUserDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }


    private PayUserDetails commonFilter(HttpServletRequest request) {


        String authToken = request.getHeader(CS.ACCESS_TOKEN_NAME);
        if (StringUtils.isEmpty(authToken)) {
            authToken = request.getParameter(CS.ACCESS_TOKEN_NAME);
        }
        if (StringUtils.isEmpty(authToken)) {
            return null; //放行,并交给UsernamePasswordAuthenticationFilter进行验证,返回公共错误信息.
        }

        JWTPayload jwtPayload = JWTUtil.parseToken(authToken, SpringBeansUtil.getBean(SystemYmlConfig.class).getJwtSecret());  //反解析token信息
        //token字符串解析失败
        if (jwtPayload == null || StringUtils.isEmpty(jwtPayload.getCacheKey())) {
            return null;
        }

        //根据用户名查找数据库
        PayUserDetails jwtBaseUser = RedisUtil.getObject(jwtPayload.getCacheKey(), PayUserDetails.class);
        if (jwtBaseUser == null) {
            RedisUtil.del(jwtPayload.getCacheKey());
            return null; //数据库查询失败，删除redis
        }

        //续签时间
        RedisUtil.expire(jwtPayload.getCacheKey(), CS.TOKEN_TIME);

        return jwtBaseUser;
    }


}
