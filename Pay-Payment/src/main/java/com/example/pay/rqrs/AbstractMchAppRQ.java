package com.example.pay.rqrs;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 通用RQ, 包含mchNo和appId 必填项
 */
@Data
public class AbstractMchAppRQ extends AbstractRQ {

    /**
     * 商户号
     **/
    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    /**
     * 商户应用ID
     **/
    @NotBlank(message = "商户应用ID不能为空")
    private String appId;


}
