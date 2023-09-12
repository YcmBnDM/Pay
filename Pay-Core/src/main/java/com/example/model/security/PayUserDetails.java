package com.example.model.security;

import com.example.entity.SysUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;


/**
 * 实现 Spring Security 的 UserDetails 接口
 */
@Data
@Slf4j
public class PayUserDetails implements UserDetails {

    /**
     * 系统用户信息
     **/
    private SysUser sysUser;

    /**
     * 密码
     **/
    private String credential;

    /**
     * 角色+权限 集合   （角色必须以： ROLE_ 开头）
     **/
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

    /**
     * 缓存标志
     **/
    private String cacheKey;

    /**
     * 登录IP
     **/
    private String loginIp;

    // 无参构造，为json反序列化提供
    public PayUserDetails() {
    }

    public PayUserDetails(SysUser sysUser, String credential) {
        this.setSysUser(sysUser);
        this.setCredential(credential);
        //做一些初始化操作
    }


    /**
     * spring-security 需要验证的密码
     **/
    @Override
    public String getPassword() {
        return getCredential();
    }

    /**
     * spring-security 登录名
     **/
    @Override
    public String getUsername() {
        return getSysUser().getSysUserId() + "";
    }

    /**
     * 账户是否过期
     **/
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否已解锁
     **/
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 密码是否未过期
     **/
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否开启
     **/
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 获取权限集合
     **/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public static PayUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        try {
            return (PayUserDetails) authentication.getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }
}
