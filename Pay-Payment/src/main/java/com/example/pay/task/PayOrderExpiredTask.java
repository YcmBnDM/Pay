package com.example.pay.task;

import com.example.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;


/**
 * 订单过期定时任务
 */
@Slf4j
public class PayOrderExpiredTask {
    @Resource
    private PayOrderService payOrderService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        int updateCount = payOrderService.updateOrderExpired();
        log.info("处理订单超时{}条.", updateCount);
    }


}