package com.example.pay.rqrs.req.payorder.payway.union;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.CommonPayDataRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付方式： UPACP_BAR
 */
@Data
public class UpBarOrderRQ extends CommonPayDataRQ {

    /**
     * 用户 支付条码
     **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /**
     * 构造函数
     **/
    public UpBarOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.UP_BAR);
    }

}
