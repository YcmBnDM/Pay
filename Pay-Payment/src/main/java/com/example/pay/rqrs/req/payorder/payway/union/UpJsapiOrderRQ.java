package com.example.pay.rqrs.req.payorder.payway.union;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付方式： UP_JSAPI
 */
@Data
public class UpJsapiOrderRQ extends UnifiedOrderRQ {

    /**
     * 支付宝用户ID
     **/
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 构造函数
     **/
    public UpJsapiOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_JSAPI);
    }

    @Override
    public String getChannelUserId() {
        return this.userId;
    }

}
