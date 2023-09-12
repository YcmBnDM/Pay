package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.MchNotifyRecord;
import com.example.service.mapper.MchNotifyRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 商户通知表 服务实现类
 */
@Service
public class MchNotifyRecordService extends ServiceImpl<MchNotifyRecordMapper, MchNotifyRecord> {

    /**
     * 根据订单号和类型查询
     */
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType) {
        return getOne(
                MchNotifyRecord.gw().eq(MchNotifyRecord::getOrderId, orderId).eq(MchNotifyRecord::getOrderType, orderType)
        );
    }

    /**
     * 查询支付订单
     */
    public MchNotifyRecord findByPayOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_PAY_ORDER);
    }

    /**
     * 查询退款订单订单
     */
    public MchNotifyRecord findByRefundOrder(String orderId) {
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_REFUND_ORDER);
    }

    /**
     * 查询退款订单订单
     */
    public MchNotifyRecord findByTransferOrder(String transferId) {
        return findByOrderAndType(transferId, MchNotifyRecord.TYPE_TRANSFER_ORDER);
    }

    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult) {
        return baseMapper.updateNotifyResult(notifyId, state, resResult);
    }


}
