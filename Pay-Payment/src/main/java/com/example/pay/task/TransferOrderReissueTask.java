package com.example.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.TransferOrder;
import com.example.pay.service.TransferOrderReissueService;
import com.example.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 转账补单定时任务
 */
@Slf4j
@Component
public class TransferOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Resource
    private TransferOrderService transferOrderService;
    @Resource
    private TransferOrderReissueService transferOrderReissueService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //查询条件：
        LambdaQueryWrapper<TransferOrder> lambdaQueryWrapper = TransferOrder.gw()
                .eq(TransferOrder::getState, TransferOrder.STATE_ING) // 转账中
                .ge(TransferOrder::getCreatedAt, DateUtil.offsetDay(new Date(), -1)); // 只查询一天内的转账单;

        int currentPageIndex = 1; //当前页码
        while (true) {

            try {
                IPage<TransferOrder> iPage = transferOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if (iPage == null || iPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    break;
                }

                for (TransferOrder transferOrder : iPage.getRecords()) {
                    transferOrderReissueService.processOrder(transferOrder);
                }

                //已经到达页码最大量，无需再次查询
                if (iPage.getPages() <= currentPageIndex) {
                    break;
                }
                currentPageIndex++;

            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }
        }
    }


}
