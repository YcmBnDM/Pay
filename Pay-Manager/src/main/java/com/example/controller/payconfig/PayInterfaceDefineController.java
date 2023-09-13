package com.example.controller.payconfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.aop.MethodLog;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.PayInterfaceConfig;
import com.example.entity.PayInterfaceDefine;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.service.impl.PayInterfaceConfigService;
import com.example.service.impl.PayInterfaceDefineService;
import com.example.service.impl.PayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

;

/**
 * 支付接口定义管理类
 */
@Api(tags = "支付接口配置")
@RestController
@RequestMapping("api/payIfDefines")
public class PayInterfaceDefineController extends CommonCtrl {

    @Resource
    private PayInterfaceDefineService payInterfaceDefineService;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private PayInterfaceConfigService payInterfaceConfigService;

    @ApiOperation("支付接口--列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header")
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_LIST')")
    @GetMapping
    public ApiRes<List<PayInterfaceDefine>> list() {

        List<PayInterfaceDefine> list = payInterfaceDefineService.list(PayInterfaceDefine.gw()
                .orderByAsc(PayInterfaceDefine::getCreatedAt)
        );
        return ApiRes.ok(list);
    }


    @ApiOperation("支付接口--详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @GetMapping("/{ifCode}")
    public ApiRes detail(@PathVariable("ifCode") String ifCode) {
        return ApiRes.ok(payInterfaceDefineService.getById(ifCode));
    }

    @ApiOperation("支付接口--新增")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true),
            @ApiImplicitParam(name = "configPageType", value = "支付参数配置页面类型:1-JSON渲染,2-自定义", required = true),
            @ApiImplicitParam(name = "icon", value = "页面展示：卡片-图标"),
            @ApiImplicitParam(name = "bgColor", value = "页面展示：卡片-背景色"),
            @ApiImplicitParam(name = "ifName", value = "接口名称", required = true),
            @ApiImplicitParam(name = "isIsvMode", value = "是否支持服务商子商户模式: 0-不支持, 1-支持", required = true),
            @ApiImplicitParam(name = "isMchMode", value = "是否支持普通商户模式: 0-不支持, 1-支持", required = true),
            @ApiImplicitParam(name = "isvParams", value = "ISV接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "isvsubMchParams", value = "特约商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "normalMchParams", value = "普通商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "remark", value = "备注", required = true),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", required = true),
            @ApiImplicitParam(name = "wayCodeStrs", value = "接口类型代码（若干个接口类型代码用英文逗号拼接起来）", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付接口")
    public ApiRes add() {
        PayInterfaceDefine payInterfaceDefine = getObject(PayInterfaceDefine.class);

        JSONArray jsonArray = new JSONArray();
        String[] wayCodes = getValStringRequired("wayCodeStrs").split(",");
        for (String wayCode : wayCodes) {
            JSONObject object = new JSONObject();
            object.put("wayCode", wayCode);
            jsonArray.add(object);
        }
        payInterfaceDefine.setWayCodes(jsonArray);

        boolean result = payInterfaceDefineService.save(payInterfaceDefine);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    @ApiOperation("支付接口--更新")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true),
            @ApiImplicitParam(name = "configPageType", value = "支付参数配置页面类型:1-JSON渲染,2-自定义", required = true),
            @ApiImplicitParam(name = "icon", value = "页面展示：卡片-图标"),
            @ApiImplicitParam(name = "bgColor", value = "页面展示：卡片-背景色"),
            @ApiImplicitParam(name = "ifName", value = "接口名称", required = true),
            @ApiImplicitParam(name = "isIsvMode", value = "是否支持服务商子商户模式: 0-不支持, 1-支持", required = true),
            @ApiImplicitParam(name = "isMchMode", value = "是否支持普通商户模式: 0-不支持, 1-支持", required = true),
            @ApiImplicitParam(name = "isvParams", value = "ISV接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "isvsubMchParams", value = "特约商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "normalMchParams", value = "普通商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @ApiImplicitParam(name = "remark", value = "备注", required = true),
            @ApiImplicitParam(name = "state", value = "状态: 0-停用, 1-启用", required = true),
            @ApiImplicitParam(name = "wayCodeStrs", value = "接口类型代码（若干个接口类型代码用英文逗号拼接起来）", required = true),
            @ApiImplicitParam(name = "wayCodes", value = "接口类型代码列表")
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_EDIT')")
    @PutMapping("/{ifCode}")
    @MethodLog(remark = "更新支付接口")
    public ApiRes update(@PathVariable("ifCode") String ifCode) {
        PayInterfaceDefine payInterfaceDefine = getObject(PayInterfaceDefine.class);
        payInterfaceDefine.setIfCode(ifCode);

        JSONArray jsonArray = new JSONArray();
        String[] wayCodes = getValStringRequired("wayCodeStrs").split(",");
        for (String wayCode : wayCodes) {
            JSONObject object = new JSONObject();
            object.put("wayCode", wayCode);
            jsonArray.add(object);
        }
        payInterfaceDefine.setWayCodes(jsonArray);

        boolean result = payInterfaceDefineService.updateById(payInterfaceDefine);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @ApiOperation("支付接口--删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_DEL')")
    @DeleteMapping("/{ifCode}")
    @MethodLog(remark = "删除支付接口")
    public ApiRes delete(@PathVariable("ifCode") String ifCode) {

        // 校验该支付方式是否有服务商或商户配置参数或者已有订单
        if (payInterfaceConfigService.count(PayInterfaceConfig.gw().eq(PayInterfaceConfig::getIfCode, ifCode)) > 0
                || payOrderService.count(PayOrder.gw().eq(PayOrder::getIfCode, ifCode)) > 0) {
            throw new BizException("该支付接口已有服务商或商户配置参数或已发生交易，无法删除！");
        }

        boolean result = payInterfaceDefineService.removeById(ifCode);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }

}
