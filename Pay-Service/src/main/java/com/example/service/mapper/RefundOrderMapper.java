package com.example.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.RefundOrder;

/**
 * 退款订单表 Mapper 接口
 *
 */
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {

    /** 查询全部退成功金额 **/
    Long sumSuccessRefundAmount(String payOrderId);

}
