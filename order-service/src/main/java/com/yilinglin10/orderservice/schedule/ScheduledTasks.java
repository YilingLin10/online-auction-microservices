package com.yilinglin10.orderservice.schedule;

import com.yilinglin10.orderservice.OrderServiceApplication;
import com.yilinglin10.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private OrderService orderService;
    static Logger logger = Logger.getLogger(OrderServiceApplication.class.getSimpleName());

    @Scheduled(cron = "0 * * * * *")
    public void checkOrderDeadlineReached() {
        LocalDateTime currTime = LocalDateTime.now();
        log.info("Check if any order has reached its deadline, time: {}", currTime);
        orderService.checkOrderDeadline(currTime);
    }
}
