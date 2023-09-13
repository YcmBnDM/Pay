
package com.example.pay.controller.transfer;

import com.example.entity.TransferOrder;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.controller.ApiController;
import com.example.pay.rqrs.req.transfer.QueryTransferOrderRQ;
import com.example.pay.rqrs.resp.transfer.QueryTransferOrderRS;
import com.example.pay.service.ConfigContextQueryService;
import com.example.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

;

/**
* 商户转账单查询controller
*
*/
@Slf4j
@RestController
public class QueryTransferOrderController extends ApiController {

    @Resource
    private TransferOrderService transferOrderService;
    @Resource private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/transfer/query")
    public ApiRes queryTransferOrder(){

        //获取参数 & 验签
        QueryTransferOrderRQ rq = getRQByWithMchSign(QueryTransferOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getTransferId())){
            throw new BizException("mchOrderNo 和 transferId不能同时为空");
        }

        TransferOrder refundOrder = transferOrderService.queryMchOrder(rq.getMchNo(), rq.getMchOrderNo(), rq.getTransferId());
        if(refundOrder == null){
            throw new BizException("订单不存在");
        }

        QueryTransferOrderRS bizRes = QueryTransferOrderRS.buildByRecord(refundOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }
}
