package com.example.controller.payconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.aop.MethodLog;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.MchPayPassage;
import com.example.entity.PayOrder;
import com.example.entity.PayWay;
import com.example.exception.BizException;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.impl.MchPayPassageService;
import com.example.service.impl.PayOrderService;
import com.example.service.impl.PayWayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 支付方式管理类
 */
@Api(tags = "支付方式配置")
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {

    @Resource
    PayWayService payWayService;
    @Resource
    MchPayPassageService mchPayPassageService;
    @Resource
    PayOrderService payOrderService;

    @ApiOperation("支付方式--列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码"),
            @ApiImplicitParam(name = "wayName", value = "支付方式名称")
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_WAY_LIST', 'ENT_PAY_ORDER_SEARCH_PAY_WAY')")
    @GetMapping
    public ApiPageRes<PayWay> list() {

        PayWay queryObject = getObject(PayWay.class);

        LambdaQueryWrapper<PayWay> condition = PayWay.gw();
        if (StringUtils.isNotEmpty(queryObject.getWayCode())) {
            condition.like(PayWay::getWayCode, queryObject.getWayCode());
        }
        if (StringUtils.isNotEmpty(queryObject.getWayName())) {
            condition.like(PayWay::getWayName, queryObject.getWayName());
        }
        condition.orderByAsc(PayWay::getWayCode);

        IPage<PayWay> pages = payWayService.page(getIPage(true), condition);

        return ApiPageRes.pages(pages);
    }


    @ApiOperation("支付方式--详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_WAY_VIEW', 'ENT_PC_WAY_EDIT')")
    @GetMapping("/{wayCode}")
    public ApiRes detail(@PathVariable("wayCode") String wayCode) {
        return ApiRes.ok(payWayService.getById(wayCode));
    }


    @ApiOperation("支付方式--新增")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码", required = true),
            @ApiImplicitParam(name = "wayName", value = "支付方式名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付方式")
    public ApiRes add() {
        PayWay payWay = getObject(PayWay.class);

        if (payWayService.count(PayWay.gw().eq(PayWay::getWayCode, payWay.getWayCode())) > 0) {
            throw new BizException("支付方式代码已存在");
        }
        payWay.setWayCode(payWay.getWayCode().toUpperCase());

        boolean result = payWayService.save(payWay);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    @ApiOperation("支付方式--更新")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码", required = true),
            @ApiImplicitParam(name = "wayName", value = "支付方式名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @PutMapping("/{wayCode}")
    @MethodLog(remark = "更新支付方式")
    public ApiRes update(@PathVariable("wayCode") String wayCode) {
        PayWay payWay = getObject(PayWay.class);
        payWay.setWayCode(wayCode);
        boolean result = payWayService.updateById(payWay);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @ApiOperation("支付方式--删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_DEL')")
    @DeleteMapping("/{wayCode}")
    @MethodLog(remark = "删除支付方式")
    public ApiRes delete(@PathVariable("wayCode") String wayCode) {

        // 校验该支付方式是否有商户已配置通道或者已有订单
        if (mchPayPassageService.count(MchPayPassage.gw().eq(MchPayPassage::getWayCode, wayCode)) > 0
                || payOrderService.count(PayOrder.gw().eq(PayOrder::getWayCode, wayCode)) > 0) {
            throw new BizException("该支付方式已有商户配置通道或已发生交易，无法删除！");
        }

        boolean result = payWayService.removeById(wayCode);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }


}
