package com.example.pay.rqrs.req.payorder.payway.ysf;

import com.example.constants.CS;
import com.example.pay.rqrs.req.payorder.UnifiedOrderRQ;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付方式： YSF_BAR
 */
@Data
public class YsfBarOrderRQ extends UnifiedOrderRQ {

    /**
     * 用户 支付条码
     **/
    @NotBlank(message = "支付条码不能为空")
    private String authCode;

    /**
     * 构造函数
     **/
    public YsfBarOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.YSF_BAR);
    }

}
