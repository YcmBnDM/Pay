package com.example.pay.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RefundOrder;
import com.example.pay.service.ChannelOrderReissueService;
import com.example.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 补单定时任务(退款单)
 *
 */
@Slf4j
@Component
public class RefundOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Resource
    private RefundOrderService refundOrderService;
    @Resource
    private ChannelOrderReissueService channelOrderReissueService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //查询条件： 退款中的订单
        LambdaQueryWrapper<RefundOrder> lambdaQueryWrapper = RefundOrder.gw().eq(RefundOrder::getState, RefundOrder.STATE_ING);

        int currentPageIndex = 1; //当前页码
        while (true) {

            try {
                IPage<RefundOrder> refundOrderIPage = refundOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if (refundOrderIPage == null || refundOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    break;
                }

                for (RefundOrder refundOrder : refundOrderIPage.getRecords()) {
                    channelOrderReissueService.processRefundOrder(refundOrder);
                }

                //已经到达页码最大量，无需再次查询
                if (refundOrderIPage.getPages() <= currentPageIndex) {
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
