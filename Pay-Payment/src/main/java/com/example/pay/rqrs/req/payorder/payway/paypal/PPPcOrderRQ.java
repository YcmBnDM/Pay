package com.example.pay.rqrs.req.payorder.payway.paypal;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.rqrs.payorder.payway
 * @create 2021/11/15 17:52
 */
@Data
public class PPPcOrderRQ extends CommonPayDataRQ {

    /**
     * 商品描述信息
     **/
    @NotBlank(message = "取消支付返回站点")
    private String cancelUrl;

    public PPPcOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.PP_PC);
    }
}
