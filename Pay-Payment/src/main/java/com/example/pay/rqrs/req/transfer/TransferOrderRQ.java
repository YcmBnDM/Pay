
package com.example.pay.rqrs.req.transfer;

import com.example.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/*
* 申请转账 请求参数
*
*/
@Data
public class TransferOrderRQ extends AbstractMchAppRQ {

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 支付接口代码   **/
    @NotBlank(message="支付接口代码不能为空")
    private String ifCode;

    /** 入账方式  **/
    @NotBlank(message="入账方式不能为空")
    private String entryType;

    /** 支付金额， 单位：分 **/
    @NotNull(message="转账金额不能为空")
    @Min(value = 1, message = "转账金额不能小于1分")
    private Long amount;

    /** 货币代码 **/
    @NotBlank(message="货币代码不能为空")
    private String currency;

    /** 收款账号 **/
    @NotBlank(message="收款账号不能为空")
    private String accountNo;

    /** 收款人姓名 **/
    private String accountName;

    /** 收款人开户行名称 **/
    private String bankName;

    /** 客户端IP地址 **/
    private String clientIp;

    /** 转账备注信息 **/
    @NotBlank(message="转账备注信息不能为空")
    private String transferDesc;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 特定渠道发起额外参数 **/
    private String channelExtra;

    /** 商户扩展参数 **/
    private String extParam;

}
