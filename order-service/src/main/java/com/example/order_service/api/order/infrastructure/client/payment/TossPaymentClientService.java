package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TossPaymentClientService {
    private final TossPaymentClient tossPaymentClient;

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "confirmPaymentFallback")
    public TossPaymentConfirmResponse confirmPayment(Long orderId, String paymentKey, Long amount){
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(orderId, paymentKey, amount);
        return tossPaymentClient.confirmPayment(request);
    }

    private TossPaymentConfirmResponse confirmPaymentFallback(Long orderId, String paymentKey, Long amount, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            log.warn("토스 서킷 브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof PaymentException) {
            throw (PaymentException) throwable;
        }
        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
