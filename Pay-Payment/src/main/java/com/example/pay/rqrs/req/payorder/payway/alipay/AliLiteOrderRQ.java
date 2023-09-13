package com.example.pay.rqrs.req.payorder.payway.alipay;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付方式： ALI_LITE
 */
@Data
public class AliLiteOrderRQ extends UnifiedOrderRQ {

    /**
     * 支付宝用户ID
     **/
    @NotBlank(message = "用户ID不能为空")
    private String buyerUserId;

    /**
     * 构造函数
     **/
    public AliLiteOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.ALI_LITE);
    }

    @Override
    public String getChannelUserId() {
        return this.buyerUserId;
    }

}
