
package com.example.pay.controller.refund;

import com.example.entity.RefundOrder;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.controller.ApiController;
import com.example.pay.rqrs.req.refund.QueryRefundOrderRQ;
import com.example.pay.rqrs.resp.refund.QueryRefundOrderRS;
import com.example.pay.service.ConfigContextQueryService;
import com.example.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;



/**
* 商户退款单查询controller
*
*/
@Slf4j
@RestController
public class QueryRefundOrderController extends ApiController {

    @Resource
    private RefundOrderService refundOrderService;
    @Resource private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/refund/query")
    public ApiRes queryRefundOrder(){

        //获取参数 & 验签
        QueryRefundOrderRQ rq = getRQByWithMchSign(QueryRefundOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchRefundNo(), rq.getRefundOrderId())){
            throw new BizException("mchRefundNo 和 refundOrderId不能同时为空");
        }

        RefundOrder refundOrder = refundOrderService.queryMchOrder(rq.getMchNo(), rq.getMchRefundNo(), rq.getRefundOrderId());
        if(refundOrder == null){
            throw new BizException("订单不存在");
        }

        QueryRefundOrderRS bizRes = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }
}
