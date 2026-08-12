package com.example.order_service.order.adapter.in.scheduler;

import com.example.order_service.order.application.service.saga.SagaCleanupProcessor;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSagaScheduler {

    private final SagaCleanupProcessor processor;

    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    @SchedulerLock(
            name = "sagaTimeoutRecoveryLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT4M"
    )
    public void recoverTimeoutSagaInstances() {
        processor.cleanupTimeoutSaga();
    }
}
