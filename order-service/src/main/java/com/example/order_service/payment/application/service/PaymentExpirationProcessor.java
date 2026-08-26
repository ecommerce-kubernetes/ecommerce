package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGInquiryResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.config.PaymentProperties;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
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
    private final PaymentPGPort pgPort;

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

    public void processTimeoutApprovePendingPayments(LocalDateTime currentTime) {
        LocalDateTime timeoutThreshold = currentTime.minusMinutes(paymentProperties.timeoutApprovePending());

        List<PaymentResult> timeoutPayments = paymentQueryService.getPaymentsByApprovePendingAndUpdatedAtBefore(timeoutThreshold);

        if (timeoutPayments.isEmpty()) {
            return;
        }

        log.info("[PaymentTimeoutScheduler] APPROVE_PENDING 타임아웃 대상 결제 건수: {}", timeoutPayments.size());

        for (PaymentResult payment : timeoutPayments) {
            try {
                PGInquiryResult inquiry = pgPort.inquiry(payment.paymentKey());
                PaymentFailure failure = determineFailureAndProcessCancel(payment, inquiry);
                paymentCommandService.abort(payment.paymentId(), failure);
            } catch (Exception e) {
                log.error("[PaymentTimeoutScheduler] 결제 실패 처리 실패 - paymentId: {}", payment.paymentId(), e);
            }
        }
    }

    private PaymentFailure determineFailureAndProcessCancel(PaymentResult payment, PGInquiryResult inquiry) {
        return switch (inquiry.status()) {
            case DONE -> {
                pgPort.netCancel(payment.paymentKey(), "결제 승인 대기 만료로 인한 결제 취소", payment.provider());
                yield PaymentFailure.of("APPROVE_TIMEOUT", "결제 승인 시간 만료(망취소 완료)");
            }
            case ABORTED -> PaymentFailure.of(inquiry.failure().code(), inquiry.failure().message());
            case CANCELED -> PaymentFailure.of("APPROVE_TIMEOUT", inquiry.cancelReason());
            case READY, IN_PROGRESS, EXPIRED -> PaymentFailure.of("APPROVE_TIMEOUT", "결제 승인 시간 만료");
            default -> PaymentFailure.of("UNKNOWN_ERROR", "알 수 없는 PG 상태");
        };
    }
}
