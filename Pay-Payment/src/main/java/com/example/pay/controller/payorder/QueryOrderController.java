
package com.example.pay.controller.payorder;

import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.controller.ApiController;
import com.example.pay.rqrs.req.payorder.QueryPayOrderRQ;
import com.example.pay.rqrs.resp.payorder.QueryPayOrderRS;
import com.example.pay.service.ConfigContextQueryService;
import com.example.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

;

/*
* 商户查单controller
*
*/
@Slf4j
@RestController
public class QueryOrderController extends ApiController {

    @Resource
    private PayOrderService payOrderService;
    @Resource private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/pay/query")
    public ApiRes queryOrder(){

        //获取参数 & 验签
        QueryPayOrderRQ rq = getRQByWithMchSign(QueryPayOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())){
            throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
        }

        PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());
        if(payOrder == null){
            throw new BizException("订单不存在");
        }

        QueryPayOrderRS bizRes = QueryPayOrderRS.buildByPayOrder(payOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }

}
