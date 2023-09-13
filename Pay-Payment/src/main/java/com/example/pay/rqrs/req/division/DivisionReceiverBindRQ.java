package com.example.pay.rqrs.req.division;

import com.example.pay.rqrs.AbstractMchAppRQ;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 分账账号的绑定 请求参数
 */
@Data
public class DivisionReceiverBindRQ extends AbstractMchAppRQ {

    /**
     * 支付接口代码
     **/
    @NotBlank(message = "支付接口代码不能为空")
    private String ifCode;

    /**
     * 接收者账号别名
     **/
    private String receiverAlias;

    /**
     * 组ID
     **/
    @NotNull(message = "组ID不能为空， 若不存在请先登录商户平台进行创建操作")
    private Long receiverGroupId;

    /**
     * 分账接收账号类型: 0-个人(对私) 1-商户(对公)
     **/
    @NotNull(message = "分账接收账号类型不能为空")
    @Range(min = 0, max = 1, message = "分账接收账号类型设置有误")
    private Byte accType;

    /**
     * 分账接收账号
     **/
    @NotBlank(message = "分账接收账号不能为空")
    private String accNo;

    /**
     * 分账接收账号名称
     **/
    private String accName;

    /**
     * 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等
     **/
    @NotBlank(message = "分账关系类型不能为空")
    private String relationType;

    /**
     * 当选择自定义时，需要录入该字段。 否则为对应的名称
     **/
    private String relationTypeName;

    /**
     * 渠道特殊信息
     */
    private String channelExtInfo;

    /**
     * 分账比例
     **/
    @NotBlank(message = "分账比例不能为空")
    private String divisionProfit;

}
