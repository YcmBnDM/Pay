package com.example.controller.sysuser;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.aop.MethodLog;
import com.example.constants.CS;
import com.example.controller.CommonCtrl;
import com.example.entity.SysRole;
import com.example.entity.SysUserRoleRela;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.AuthService;
import com.example.service.impl.SysRoleEntRelaService;
import com.example.service.impl.SysRoleService;
import com.example.service.impl.SysUserRoleRelaService;
import com.example.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

;

/**
 * 角色管理
 */
@Api(tags = "系统管理（用户角色）")
@RestController
@RequestMapping("api/sysRoles")
public class SysRoleController extends CommonCtrl {

    @Resource
    SysRoleService sysRoleService;
    @Resource
    SysUserRoleRelaService sysUserRoleRelaService;
    @Resource
    private AuthService authService;
    @Resource
    private SysRoleEntRelaService sysRoleEntRelaService;


    /**
     * list
     */
    @ApiOperation("角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "roleId", value = "角色ID, ROLE_开头"),
            @ApiImplicitParam(name = "roleName", value = "角色名称")
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_LIST', 'ENT_UR_USER_UPD_ROLE' )")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysRole> list() {

        SysRole queryObject = getObject(SysRole.class);

        QueryWrapper<SysRole> condition = new QueryWrapper<>();
        LambdaQueryWrapper<SysRole> lambdaCondition = condition.lambda();
        lambdaCondition.eq(SysRole::getSysType, CS.SYS_TYPE.MGR);
        lambdaCondition.eq(SysRole::getBelongInfoId, 0);

        if (StringUtils.isNotEmpty(queryObject.getRoleName())) {
            lambdaCondition.like(SysRole::getRoleName, queryObject.getRoleName());
        }

        if (StringUtils.isNotEmpty(queryObject.getRoleId())) {
            lambdaCondition.like(SysRole::getRoleId, queryObject.getRoleId());
        }

        //是否有排序字段
        MutablePair<Boolean, String> orderInfo = getSortInfo();
        if (orderInfo != null) {
            condition.orderBy(true, orderInfo.getLeft(), orderInfo.getRight());
        } else {
            lambdaCondition.orderByDesc(SysRole::getUpdatedAt);
        }

        IPage<SysRole> pages = sysRoleService.page(getIPage(true), condition);
        return ApiPageRes.pages(pages);
    }


    /**
     * detail
     */
    @ApiOperation("角色详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "角色ID, ROLE_开头", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") String recordId) {
        return ApiRes.ok(sysRoleService.getById(recordId));
    }

    /**
     * add
     */
    @ApiOperation("添加角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "roleName", value = "角色名称", required = true),
            @ApiImplicitParam(name = "entIdListStr", value = "权限信息集合，eg：[str1,str2]，字符串列表转成json字符串，若为空，则创建的角色无任何权限")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ADD' )")
    @MethodLog(remark = "添加角色信息")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes add() {
        SysRole SysRole = getObject(SysRole.class);
        String roleId = "ROLE_" + StringUtil.getUUID(6);
        SysRole.setRoleId(roleId);
        SysRole.setSysType(CS.SYS_TYPE.MGR); //后台系统
        sysRoleService.save(SysRole);

        //权限信息集合
        String entIdListStr = getValString("entIdListStr");

        //如果包含： 可分配权限的权限 && entIdListStr 不为空
        if (getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
                && StringUtils.isNotEmpty(entIdListStr)) {
            List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);

            sysRoleEntRelaService.resetRela(roleId, entIdList);
        }

        return ApiRes.ok();
    }

    /**
     * update
     */
    @ApiOperation("更新角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "角色ID, ROLE_开头", required = true),
            @ApiImplicitParam(name = "roleName", value = "角色名称", required = true),
            @ApiImplicitParam(name = "entIdListStr", value = "权限信息集合，eg：[str1,str2]，字符串列表转成json字符串，若为空，则创建的角色无任何权限")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.PUT)
    @MethodLog(remark = "更新角色信息")
    public ApiRes update(@PathVariable("recordId") String recordId) {
        SysRole SysRole = getObject(SysRole.class);
        SysRole.setRoleId(recordId);
        sysRoleService.updateById(SysRole);

        //权限信息集合
        String entIdListStr = getValString("entIdListStr");

        //如果包含： 可分配权限的权限 && entIdListStr 不为空
        if (getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
                && StringUtils.isNotEmpty(entIdListStr)) {
            List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);

            sysRoleEntRelaService.resetRela(recordId, entIdList);

            List<Long> sysUserIdList = new ArrayList<>();
            sysUserRoleRelaService.list(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)).stream().forEach(item -> sysUserIdList.add(item.getUserId()));

            //查询到该角色的人员， 将redis更新
            authService.refAuthentication(sysUserIdList);
        }

        return ApiRes.ok();
    }

    /**
     * delete
     */
    @ApiOperation("删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "recordId", value = "角色ID, ROLE_开头", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_UR_ROLE_DEL')")
    @MethodLog(remark = "删除角色")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.DELETE)
    public ApiRes del(@PathVariable("recordId") String recordId) {

        if (sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)) > 0) {
            throw new BizException("当前角色已分配到用户, 不可删除！");
        }
        sysRoleService.removeRole(recordId);
        return ApiRes.ok();
    }

}
