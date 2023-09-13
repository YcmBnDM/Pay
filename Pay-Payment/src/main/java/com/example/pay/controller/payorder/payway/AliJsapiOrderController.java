
package com.example.pay.controller.payorder.payway;

import com.example.constants.CS;
import com.example.model.ApiRes;
import com.example.pay.controller.payorder.AbstractPayOrderController;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliJsapiOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 支付宝 jspai controller

 */
@Slf4j
@RestController
public class AliJsapiOrderController extends AbstractPayOrderController {


    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/aliJsapiOrder")
    public ApiRes aliJsapiOrder(){

        //获取参数 & 验证
        AliJsapiOrderRQ bizRQ = getRQByWithMchSign(AliJsapiOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.ALI_JSAPI, bizRQ);

    }


}
