package com.example.order_service.order.adapter.in.scheduler;

import com.example.order_service.order.application.service.order.OrderExpirationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderExpirationProcessor expirationService;

    @Scheduled(cron = "0 0/5 * * * *")
    @SchedulerLock(
            name = "order_timeout_scheduler_lock",
            lockAtLeastFor = "2m",
            lockAtMostFor = "10m"
    )
    public void scheduleTimeoutOrders() {
        log.info("[OrderScheduler] 주문 타임아웃 배치 시작");

        expirationService.processTimeoutOrders(LocalDateTime.now());

        log.info("[OrderScheduler] 주문 타임아웃 배치 종료");
    }
}
