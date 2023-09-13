package com.example.controller.sysuser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.controller.CommonCtrl;
import com.example.entity.SysEntitlement;
import com.example.model.ApiRes;
import com.example.service.impl.SysEntitlementService;
import com.example.util.other.TreeDataBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

;

/**
 * 权限菜单管理类
 */
@Api(tags = "系统管理（用户权限）")
@RestController
@RequestMapping("api/sysEnts")
public class SysEntController extends CommonCtrl {

    @Resource
    SysEntitlementService sysEntitlementService;

    /**
     * 查询权限集合
     */
    @ApiOperation("查询权限集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "sysType", value = "所属系统： MGR-运营平台, MCH-商户中心", required = true)
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST', 'ENT_UR_ROLE_DIST' )")
    @RequestMapping(value = "/showTree", method = RequestMethod.GET)
    public ApiRes<List<JSONObject>> showTree() {

        //查询全部数据
        List<SysEntitlement> list = sysEntitlementService.list(SysEntitlement.gw().eq(SysEntitlement::getSysType, CS.SYS_TYPE.MCH));

        //4. 转换为json树状结构
        JSONArray jsonArray = (JSONArray) JSONArray.toJSON(list);
        List<JSONObject> leftMenuTree = new TreeDataBuilder(jsonArray,
                "entId", "pid", "children", "entSort", true)
                .buildTreeObject();

        return ApiRes.ok(leftMenuTree);
    }


}
