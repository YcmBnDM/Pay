package com.example.controller;

import com.example.config.SystemYmlConfig;
import com.example.constants.ApiCodeEnum;
import com.example.constants.CS;
import com.example.ctrl.AbstractCtrl;
import com.example.entity.SysUser;
import com.example.model.ApiRes;
import com.example.model.security.PayUserDetails;
import com.example.service.impl.SysConfigService;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;


/**
 * 通用ctrl类
 */
public abstract class CommonCtrl extends AbstractCtrl {

    @Resource
    protected SystemYmlConfig mainConfig;

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 获取当前用户ID
     */
    protected PayUserDetails getCurrentUser() {

        return (PayUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取当前商户ID
     **/
    protected String getCurrentMchNo() {
        return getCurrentUser().getSysUser().getBelongInfoId();
    }

    /**
     * 获取当前用户登录IP
     *
     * @return
     */
    protected String getIp() {
        return getClientIp();
    }

    /**
     * 校验当前用户是否为超管
     *
     * @return
     */
    protected ApiRes checkIsAdmin() {
        SysUser sysUser = getCurrentUser().getSysUser();
        if (sysUser.getIsAdmin() != CS.YES) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        } else {
            return null;
        }

    }

}
