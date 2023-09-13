
package com.example.pay.rqrs.resp.refund;


import com.example.entity.RefundOrder;
import com.example.pay.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
* 查询退款单 响应参数
*
*/
@Data
public class QueryRefundOrderRS extends AbstractRS {

    /**
     * 退款订单号（支付系统生成订单号）
     */
    private String refundOrderId;

    /**
     * 支付订单号（与t_pay_order对应）
     */
    private String payOrderId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户退款单号（商户系统的订单号）
     */
    private String mchRefundNo;

    /**
     * 支付金额,单位分
     */
    private Long payAmount;

    /**
     * 退款金额,单位分
     */
    private Long refundAmount;

    /**
     * 三位货币代码,人民币:cny
     */
    private String currency;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
     */
    private Byte state;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * 渠道错误码
     */
    private String errCode;

    /**
     * 渠道错误描述
     */
    private String errMsg;

    /**
     * 扩展参数
     */
    private String extParam;

    /**
     * 订单退款成功时间
     */
    private Long successTime;

    /**
     * 创建时间
     */
    private Long createdAt;


    public static QueryRefundOrderRS buildByRefundOrder(RefundOrder refundOrder){

        if(refundOrder == null){
            return null;
        }

        QueryRefundOrderRS result = new QueryRefundOrderRS();
        BeanUtils.copyProperties(refundOrder, result);
        result.setSuccessTime(refundOrder.getSuccessTime() == null ? null : refundOrder.getSuccessTime().getTime());
        result.setCreatedAt(refundOrder.getCreatedAt() == null ? null : refundOrder.getCreatedAt().getTime());
        return result;
    }


}
