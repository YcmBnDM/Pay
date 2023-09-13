package com.example.controller.payconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.controller.CommonCtrl;
import com.example.entity.PayWay;
import com.example.model.ApiPageRes;
import com.example.service.impl.MchPayPassageService;
import com.example.service.impl.PayOrderService;
import com.example.service.impl.PayWayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * 支付方式配置类
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

    /**
     * @Description: list
     */
    @ApiOperation("支付方式列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数（-1时查全部数据）", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "wayCode", value = "支付方式代码"),
            @ApiImplicitParam(name = "wayName", value = "支付方式名称")
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_SEARCH_PAY_WAY')")
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

}
