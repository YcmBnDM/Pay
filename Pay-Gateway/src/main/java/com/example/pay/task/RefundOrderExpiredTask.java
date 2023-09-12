package com.example.pay.task;

import com.example.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 退款订单过期定时任务
 */
@Slf4j
@Component
public class RefundOrderExpiredTask {

    @Resource
    private RefundOrderService refundOrderService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        int updateCount = refundOrderService.updateOrderExpired();
        log.info("处理退款订单超时{}条.", updateCount);
    }


}
