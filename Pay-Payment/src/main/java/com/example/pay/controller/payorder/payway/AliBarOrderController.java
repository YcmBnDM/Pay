
package com.example.pay.controller.payorder.payway;

import com.example.constants.CS;
import com.example.model.ApiRes;
import com.example.pay.controller.payorder.AbstractPayOrderController;
import com.example.pay.rqrs.req.payorder.payway.alipay.AliBarOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 支付宝 条码支付 controller
*
*/
@Slf4j
@RestController
public class AliBarOrderController extends AbstractPayOrderController {


    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/aliBarOrder")
    public ApiRes aliBarOrder(){

        //获取参数 & 验证
        AliBarOrderRQ bizRQ = getRQByWithMchSign(AliBarOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.ALI_BAR, bizRQ);

    }


}
