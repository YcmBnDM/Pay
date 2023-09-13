package com.example.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.PayOrder;
import com.example.pay.service.ChannelOrderReissueService;
import com.example.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 补单定时任务
 */
@Slf4j
@Component
public class PayOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Resource
    private PayOrderService payOrderService;
    @Resource
    private ChannelOrderReissueService channelOrderReissueService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //当前时间 减去10分钟。
        Date offsetDate = DateUtil.offsetMinute(new Date(), -10);

        //查询条件： 支付中的订单 & （ 订单创建时间 + 10分钟 >= 当前时间 ）
        LambdaQueryWrapper<PayOrder> lambdaQueryWrapper = PayOrder.gw().eq(PayOrder::getState, PayOrder.STATE_ING).le(PayOrder::getCreatedAt, offsetDate);

        int currentPageIndex = 1; //当前页码
        while (true) {

            try {
                IPage<PayOrder> payOrderIPage = payOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if (payOrderIPage == null || payOrderIPage.getRecords().isEmpty()) { //本次查询无结果, 不再继续查询;
                    break;
                }

                for (PayOrder payOrder : payOrderIPage.getRecords()) {
                    channelOrderReissueService.processPayOrder(payOrder);
                }

                //已经到达页码最大量，无需再次查询
                if (payOrderIPage.getPages() <= currentPageIndex) {
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
