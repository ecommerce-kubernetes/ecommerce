package com.example.order_service.saga.adapter.in.scheduler;

import com.example.order_service.saga.application.service.OrderSagaExpirationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaScheduler {

    private final OrderSagaExpirationProcessor expirationProcessor;

    @Scheduled(cron = "0 0/3 * * * *")
    @SchedulerLock(
            name = "forward_pending_timeout_scheduler_lock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT2M"
    )
    public void scheduleTimeoutForwardPendingSagas() {
        log.info("[OrderSagaScheduler] 사가 정방향 대기 타임아웃 배치 시작");

        expirationProcessor.processTimeoutForwardPendingSagas(LocalDateTime.now());

        log.info("[OrderSagaScheduler] 사가 정방향 대기 타임아웃 배치 종료");
    }

    @Scheduled(cron = "0 0/3 * * * *")
    @SchedulerLock(
            name = "compensate_pending_timeout_scheduler_lock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT2M"
    )
    public void scheduleTimeoutCompensatePendingSagas() {
        log.info("[OrderSagaScheduler] 사가 보상 대기 타임아웃 배치 시작");

        expirationProcessor.processTimeoutCompensatePendingSagas(LocalDateTime.now());

        log.info("[OrderSagaScheduler] 사가 보상 대기 타임아웃 배치 종료");
    }
}
