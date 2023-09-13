package com.example.pay.rqrs.resp.division;

import com.example.entity.MchDivisionReceiver;
import com.example.pay.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 绑定账户 响应参数
 */
@Data
public class DivisionReceiverBindRS extends AbstractRS {


    /**
     * 分账接收者ID
     */
    private Long receiverId;

    /**
     * 接收者账号别名
     */
    private String receiverAlias;

    /**
     * 组ID（便于商户接口使用）
     */
    private Long receiverGroupId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 支付接口代码
     */
    private String ifCode;

    /**
     * 分账接收账号类型: 0-个人(对私) 1-商户(对公)
     */
    private Byte accType;

    /**
     * 分账接收账号
     */
    private String accNo;

    /**
     * 分账接收账号名称
     */
    private String accName;

    /**
     * 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等
     */
    private String relationType;

    /**
     * 当选择自定义时，需要录入该字段。 否则为对应的名称
     */
    private String relationTypeName;


    /**
     * 渠道特殊信息
     */
    private String channelExtInfo;

    /**
     * 绑定成功时间
     */
    private Long bindSuccessTime;

    /**
     * 分账比例
     */
    private BigDecimal divisionProfit;

    /**
     * 分账状态 1-绑定成功, 0-绑定异常
     */
    private Byte bindState;

    /**
     * 支付渠道错误码
     */
    private String errCode;

    /**
     * 支付渠道错误信息
     */
    private String errMsg;


    public static DivisionReceiverBindRS buildByRecord(MchDivisionReceiver record) {

        if (record == null) {
            return null;
        }

        DivisionReceiverBindRS result = new DivisionReceiverBindRS();
        BeanUtils.copyProperties(record, result);
        result.setBindSuccessTime(record.getBindSuccessTime() != null ? record.getBindSuccessTime().getTime() : null);

        return result;
    }


}
