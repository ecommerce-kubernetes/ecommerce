package com.example.order_service.order.adapter.in.scheduler;

import com.example.order_service.order.application.service.order.OrderCleanupProcessor;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderCleanupProcessor processor;

    @Scheduled(initialDelay = 60000, fixedDelay = 1800000)
    @SchedulerLock(
            name = "pendingOrderCleanupLock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT30M"
    )
    public void cleanupPendingOrders() {
        processor.cleanupExpiredPendingOrders();
    }
}
