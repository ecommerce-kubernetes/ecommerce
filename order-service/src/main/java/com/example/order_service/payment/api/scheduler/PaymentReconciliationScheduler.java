package com.example.order_service.payment.api.scheduler;

import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.PaymentReconciler;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentReconciler paymentReconciler;

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    @SchedulerLock(
            name = "paymentApprovalReconciliationLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT5M"
    )
    public void runPaymentApprovalReconciliation() {
        paymentReconciler.reconcileReadyPayments();
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 180000)
    @SchedulerLock(
            name = "paymentRefundReconciliationLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT5M"
    )
    public void runPaymentRefundReconciliation(){
        paymentReconciler.reconcileRefundPendingPayments();
    }
}
