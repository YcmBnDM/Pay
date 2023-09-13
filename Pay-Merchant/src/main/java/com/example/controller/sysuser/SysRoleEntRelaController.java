package com.example.controller.sysuser;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.SysRole;
import com.example.entity.SysRoleEntRela;
import com.example.entity.SysUserRoleRela;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.AuthService;
import com.example.service.impl.SysRoleEntRelaService;
import com.example.service.impl.SysRoleService;
import com.example.service.impl.SysUserRoleRelaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 角色权限管理类
 */
@Api(tags = "系统管理（用户-角色-权限关联信息）")
@RestController
@RequestMapping("api/sysRoleEntRelas")
public class SysRoleEntRelaController extends CommonCtrl {

    @Resource
    private SysRoleEntRelaService sysRoleEntRelaService;
    @Resource
    private SysUserRoleRelaService sysUserRoleRelaService;
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private AuthService authService;

    /**
     * list
     */
    @ApiOperation("关联关系--角色-权限关联信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "roleId", value = "角色ID, ROLE_开头")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_DIST' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysRoleEntRela> list() {

        SysRoleEntRela queryObject = getObject(SysRoleEntRela.class);

        LambdaQueryWrapper<SysRoleEntRela> condition = SysRoleEntRela.gw();

        if (queryObject.getRoleId() != null) {
            condition.eq(SysRoleEntRela::getRoleId, queryObject.getRoleId());
        }

        IPage<SysRoleEntRela> pages = sysRoleEntRelaService.page(getIPage(true), condition);

        return ApiPageRes.pages(pages);
    }

    /**
     * 重置角色权限关联信息
     */
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_DIST' )")
    @RequestMapping(value = "relas/{roleId}", method = RequestMethod.POST)
    public ApiRes relas(@PathVariable("roleId") String roleId) {

        SysRole sysRole = sysRoleService.getOne(SysRole.gw().eq(SysRole::getRoleId, roleId).eq(SysRole::getBelongInfoId, getCurrentMchNo()));
        if (sysRole == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        List<String> entIdList = JSONArray.parseArray(getValStringRequired("entIdListStr"), String.class);

        sysRoleEntRelaService.resetRela(roleId, entIdList);

        List<Long> sysUserIdList = new ArrayList<>();
        sysUserRoleRelaService.list(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, roleId)).stream().forEach(item -> sysUserIdList.add(item.getUserId()));

        //查询到该角色的人员， 将redis更新
        authService.refAuthentication(sysUserIdList);

        return ApiRes.ok();
    }

}
