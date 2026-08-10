package com.example.order_service.payment.api.scheduler;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    @SchedulerLock(
            name = "paymentApprovalReconciliationLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT5M"
    )
    public void runPaymentApprovalReconciliation() {

    }

    @Scheduled(initialDelay = 20000, fixedDelay = 180000)
    @SchedulerLock(
            name = "paymentRefundReconciliationLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT5M"
    )
    public void runPaymentRefundReconciliation(){

    }
}
