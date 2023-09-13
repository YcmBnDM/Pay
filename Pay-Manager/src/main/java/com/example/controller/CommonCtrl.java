
package com.example.controller;

import com.example.config.SystemYmlConfig;
import com.example.ctrl.AbstractCtrl;
import com.example.model.security.PayUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;

;

/**
 * 定义通用CommonCtrl
 */
public abstract class CommonCtrl extends AbstractCtrl {

    @Resource
    protected SystemYmlConfig mainConfig;

    /**
     * 获取当前用户ID
     */
    protected PayUserDetails getCurrentUser() {

        return (PayUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取当前用户登录IP
     *
     * @return
     */
    protected String getIp() {
        return getClientIp();
    }

}
