package com.example.security;

import com.example.constants.CS;
import com.example.entity.SysUser;
import com.example.entity.SysUserAuth;
import com.example.exception.JeepayAuthenticationException;
import com.example.model.security.PayUserDetails;
import com.example.service.impl.SysUserAuthService;
import com.example.service.impl.SysUserService;
import com.example.util.other.RegexUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * UserDetailsService实现类
 */
@Service
public class JeeUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysUserAuthService sysUserAuthService;

    /**
     * 此函数为： authenticationManager.authenticate(upToken) 内部调用 ;
     * 需返回 用户信息载体 / 用户密码  。
     * 用户角色+权限的封装集合 (暂时不查询， 在验证通过后再次查询，避免用户名密码输入有误导致查询资源浪费)
     **/
    @Override
    public UserDetails loadUserByUsername(String loginUsernameStr) throws UsernameNotFoundException {

        //登录方式， 默认为账号密码登录
        Byte identityType = CS.AUTH_TYPE.LOGIN_USER_NAME;
        if (RegexUtil.isMobile(loginUsernameStr)) {
            identityType = CS.AUTH_TYPE.TELPHONE; //手机号登录
        }

        //首先根据登录类型 + 用户名得到 信息
        SysUserAuth auth = sysUserAuthService.selectByLogin(loginUsernameStr, identityType, CS.SYS_TYPE.MCH);

        if (auth == null) { //没有该用户信息
            throw JeepayAuthenticationException.build("用户名/密码错误！");
        }

        //用户ID
        Long userId = auth.getUserId();

        SysUser sysUser = sysUserService.getById(userId);

        if (sysUser == null) {
            throw JeepayAuthenticationException.build("用户名/密码错误！");
        }

        if (CS.PUB_USABLE != sysUser.getState()) { //状态不合法
            throw JeepayAuthenticationException.build("用户状态不可登录，请联系管理员！");
        }

        return new PayUserDetails(sysUser, auth.getCredential());

    }
}
