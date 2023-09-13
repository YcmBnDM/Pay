
package com.example.pay.controller.payorder;

import com.example.constants.CS;
import com.example.entity.PayOrder;
import com.example.entity.PayWay;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import com.example.pay.rqrs.req.payorder.payway.AutoBarOrderRQ;
import com.example.pay.rqrs.resp.payorder.UnifiedOrderRS;
import com.example.pay.service.ConfigContextQueryService;
import com.example.service.impl.PayWayService;
import com.example.util.PayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

;

/*
* 统一下单 controller
*
*/
@Slf4j
@RestController
public class UnifiedOrderController extends AbstractPayOrderController {

    @Resource
    private PayWayService payWayService;
    @Resource private ConfigContextQueryService configContextQueryService;

    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/unifiedOrder")
    public ApiRes unifiedOrder(){

        //获取参数 & 验签
        UnifiedOrderRQ rq = getRQByWithMchSign(UnifiedOrderRQ.class);

        UnifiedOrderRQ bizRQ = buildBizRQ(rq);

        //实现子类的res
        ApiRes apiRes = unifiedOrder(bizRQ.getWayCode(), bizRQ);
        if(apiRes.getData() == null){
            return apiRes;
        }

        UnifiedOrderRS bizRes = (UnifiedOrderRS)apiRes.getData();

        //聚合接口，返回的参数
        UnifiedOrderRS res = new UnifiedOrderRS();
        BeanUtils.copyProperties(bizRes, res);

        //只有 订单生成（QR_CASHIER） || 支付中 || 支付成功返回该数据
        if(bizRes.getOrderState() != null && (bizRes.getOrderState() == PayOrder.STATE_INIT || bizRes.getOrderState() == PayOrder.STATE_ING || bizRes.getOrderState() == PayOrder.STATE_SUCCESS) ){
            res.setPayDataType(bizRes.buildPayDataType());
            res.setPayData(bizRes.buildPayData());
        }

        return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }


    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq){

        //支付方式  比如： ali_bar
        String wayCode = rq.getWayCode();

        //jsapi 收银台聚合支付场景 (不校验是否存在payWayCode)
        if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){
            return rq.buildBizRQ();
        }

        //如果是自动分类条码
        if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)){

            AutoBarOrderRQ bizRQ = (AutoBarOrderRQ)rq.buildBizRQ();
            wayCode = PayUtil.getPayWayCodeByBarCode(bizRQ.getAuthCode());
            rq.setWayCode(wayCode.trim());
        }

        if(payWayService.count(PayWay.gw().eq(PayWay::getWayCode, wayCode)) <= 0){
            throw new BizException("不支持的支付方式");
        }

        //转换为 bizRQ
        return rq.buildBizRQ();
    }


}
