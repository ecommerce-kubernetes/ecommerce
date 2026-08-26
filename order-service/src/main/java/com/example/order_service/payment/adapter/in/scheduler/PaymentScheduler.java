package com.example.order_service.payment.adapter.in.scheduler;

import com.example.order_service.payment.application.service.PaymentExpirationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentExpirationProcessor expirationService;

    @Scheduled(cron = "0 0/5 * * * *")
    @SchedulerLock(
            name = "ready_payment_timeout_scheduler_lock",
            lockAtLeastFor = "PT2M",
            lockAtMostFor = "PT10M"
    )
    public void scheduleTimeoutReadyPayments() {
        log.info("[PaymentScheduler] 결제 준비 타임아웃 배치 시작");

        expirationService.processTimeoutReadyPayments(LocalDateTime.now());

        log.info("[PaymentScheduler] 결제 준비 타임아웃 배치 종료");
    }

    @Scheduled(cron = "0 0/3 * * * *")
    @SchedulerLock(
            name = "approval_pending_reconciliation_lock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT2M"
    )
    private void scheduleApprovalPendingReconciliation() {
        log.info("[PaymentScheduler] 결제 승인 대기 타임아웃 배치 시작");

        expirationService.processTimeoutApprovePendingPayments(LocalDateTime.now());

        log.info("[PaymentScheduler] 결제 승인 대기 타임아웃 배치 종료");
    }

    @Scheduled(cron = "0 0/3 * * * *")
    @SchedulerLock(
            name = "refund_pending_reconciliation_lock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT2M"
    )
    private void scheduleRefundPendingReconciliation() {
        log.info("[PaymentScheduler] 결제 환불 대기 타임아웃 배치 시작");

        expirationService.processTimeoutRefundPendingPayments(LocalDateTime.now());

        log.info("[PaymentScheduler] 결제 환불 대기 타임아웃 배치 종료");
    }
}
