package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.config.PaymentProperties;
import com.example.order_service.payment.domain.PaymentFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExpirationProcessor {

    private final PaymentProperties paymentProperties;
    private final PaymentQueryService paymentQueryService;
    private final PaymentCommandService paymentCommandService;

    public void processTimeoutReadyPayments(LocalDateTime currentTime) {
        LocalDateTime timeoutThreshold = currentTime.minusMinutes(paymentProperties.timeoutReady());

        List<PaymentResult> timeoutPayments = paymentQueryService.getPaymentsByReadyAndCreatedAtBefore(timeoutThreshold);

        if (timeoutPayments.isEmpty()) {
            return;
        }

        log.info("[PaymentTimeoutScheduler] READY 타임아웃 대상 결제 건수: {}", timeoutPayments.size());

        for (PaymentResult payment : timeoutPayments) {
            try {
                PaymentFailure paymentFailure = PaymentFailure.of("TIMEOUT", "결제 준비 타임 아웃");
                paymentCommandService.abort(payment.paymentId(), paymentFailure);
            } catch (Exception e) {
                log.error("[PaymentTimeoutScheduler] 결제 실패 처리 실패 - paymentId: {}", payment.paymentId(), e);
            }
        }
    }
}
