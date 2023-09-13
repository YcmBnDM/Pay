package com.example.service;

import cn.hutool.core.util.IdUtil;
import com.example.cache.RedisUtil;
import com.example.cache.TokenService;
import com.example.config.SystemYmlConfig;
import com.example.constants.CS;
import com.example.entity.MchInfo;
import com.example.entity.SysUser;
import com.example.exception.BizException;
import com.example.exception.JeepayAuthenticationException;
import com.example.jwt.JWTPayload;
import com.example.jwt.JWTUtil;
import com.example.model.security.PayUserDetails;
import com.example.service.impl.MchInfoService;
import com.example.service.impl.SysRoleEntRelaService;
import com.example.service.impl.SysRoleService;
import com.example.service.impl.SysUserService;
import com.example.service.mapper.SysEntitlementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

;
;

/**
 * 认证Service
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysRoleEntRelaService sysRoleEntRelaService;
    @Resource
    private MchInfoService mchInfoService;
    @Resource
    private SysEntitlementMapper sysEntitlementMapper;
    @Resource
    private SystemYmlConfig systemYmlConfig;

    /**
     * 认证
     **/
    public String auth(String username, String password) {

        //1. 生成spring-security usernamePassword类型对象
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);

        //spring-security 自动认证过程；
        // 1. 进入 JeeUserDetailsServiceImpl.loadUserByUsername 获取用户基本信息；
        //2. SS根据UserDetails接口验证是否用户可用；
        //3. 最后返回loadUserByUsername 封装的对象信息；
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(upToken);
        } catch (JeepayAuthenticationException jex) {
            throw jex.getBizException() == null ? new BizException(jex.getMessage()) : jex.getBizException();
        } catch (BadCredentialsException e) {
            throw new BizException("用户名/密码错误！");
        } catch (AuthenticationException e) {
            log.error("AuthenticationException:", e);
            throw new BizException("认证服务出现异常， 请重试或联系系统管理员！");
        }
        PayUserDetails jeeUserDetails = (PayUserDetails) authentication.getPrincipal();

        //验证通过后 再查询用户角色和权限信息集合

        SysUser sysUser = jeeUserDetails.getSysUser();

        //非超级管理员 && 不包含左侧菜单 进行错误提示
        if (sysUser.getIsAdmin() != CS.YES && sysEntitlementMapper.userHasLeftMenu(sysUser.getSysUserId(), CS.SYS_TYPE.MCH) <= 0) {
            throw new BizException("当前用户未分配任何菜单权限，请联系管理员进行分配后再登录！");
        }

        // 查询当前用户的商户信息
        MchInfo mchInfo = mchInfoService.getById(sysUser.getBelongInfoId());
        if (mchInfo != null) {
            // 判断当前商户状态是否可用
            if (mchInfo.getState() == CS.NO) {
                throw new BizException("当前商户状态不可用！");
            }
        }
        // 放置权限集合
        jeeUserDetails.setAuthorities(getUserAuthority(sysUser));

        //生成token
        String cacheKey = CS.getCacheKeyToken(sysUser.getSysUserId(), IdUtil.fastUUID());

        //生成iToken 并放置到缓存
        TokenService.processTokenCache(jeeUserDetails, cacheKey); //处理token 缓存信息

        //将信息放置到Spring-security context中
        UsernamePasswordAuthenticationToken authenticationRest = new UsernamePasswordAuthenticationToken(jeeUserDetails, null, jeeUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationRest);

        //返回JWTToken
        return JWTUtil.generateToken(new JWTPayload(jeeUserDetails), systemYmlConfig.getJwtSecret());
    }

    /**
     * 根据用户ID 更新缓存中的权限集合， 使得分配实时生效
     **/
    public void refAuthentication(List<Long> sysUserIdList) {

        if (sysUserIdList == null || sysUserIdList.isEmpty()) {
            return;
        }

        Map<Long, SysUser> sysUserMap = new HashMap<>();

        // 查询 sysUserId 和 state
        sysUserService.list(SysUser.gw().select(SysUser::getSysUserId, SysUser::getState).in(SysUser::getSysUserId, sysUserIdList)).stream().forEach(item -> sysUserMap.put(item.getSysUserId(), item));

        for (Long sysUserId : sysUserIdList) {

            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if (cacheKeyList == null || cacheKeyList.isEmpty()) {
                continue;
            }

            for (String cacheKey : cacheKeyList) {

                //用户不存在 || 已禁用 需要删除Redis
                if (sysUserMap.get(sysUserId) == null || sysUserMap.get(sysUserId).getState() == CS.PUB_DISABLE) {
                    RedisUtil.del(cacheKey);
                    continue;
                }

                PayUserDetails jwtBaseUser = RedisUtil.getObject(cacheKey, PayUserDetails.class);
                if (jwtBaseUser == null) {
                    continue;
                }

                // 重新放置sysUser对象
                jwtBaseUser.setSysUser(sysUserService.getById(sysUserId));

                //查询放置权限数据
                jwtBaseUser.setAuthorities(getUserAuthority(jwtBaseUser.getSysUser()));

                //保存token  失效时间不变
                RedisUtil.set(cacheKey, jwtBaseUser);
            }
        }

    }

    /**
     * 根据用户ID 删除用户缓存信息
     **/
    public void delAuthentication(List<Long> sysUserIdList) {
        if (sysUserIdList == null || sysUserIdList.isEmpty()) {
            return;
        }
        for (Long sysUserId : sysUserIdList) {
            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if (cacheKeyList == null || cacheKeyList.isEmpty()) {
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                RedisUtil.del(cacheKey);
            }
        }
    }

    public List<SimpleGrantedAuthority> getUserAuthority(SysUser sysUser) {

        //用户拥有的角色集合  需要以ROLE_ 开头,  用户拥有的权限集合
        List<String> roleList = sysRoleService.findListByUser(sysUser.getSysUserId());
        List<String> entList = sysRoleEntRelaService.selectEntIdsByUserId(sysUser.getSysUserId(), sysUser.getIsAdmin(), sysUser.getSysType());

        List<SimpleGrantedAuthority> grantedAuthorities = new LinkedList<>();
        roleList.stream().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role)));
        entList.stream().forEach(ent -> grantedAuthorities.add(new SimpleGrantedAuthority(ent)));
        return grantedAuthorities;
    }


}