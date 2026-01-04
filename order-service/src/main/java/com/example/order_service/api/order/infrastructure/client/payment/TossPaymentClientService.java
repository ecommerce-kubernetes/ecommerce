package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.payment.dto.request.TossPaymentCancelRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.request.TossPaymentConfirmRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentCancelResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
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
    public TossPaymentConfirmResponse confirmPayment(String orderNo, String paymentKey, Long amount){
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(orderNo, paymentKey, amount);
        return tossPaymentClient.confirmPayment(request);
    }

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "cancelPaymentFallback")
    public TossPaymentCancelResponse cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        TossPaymentCancelRequest request = TossPaymentCancelRequest.of(cancelReason, cancelAmount);
        return tossPaymentClient.cancelPayment(paymentKey, request);
    }

    private TossPaymentCancelResponse cancelPaymentFallback(String paymentKey, String cancelReason, Long cancelAmount, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            log.warn("토스 서킷 브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        }
        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }

    private TossPaymentConfirmResponse confirmPaymentFallback(String orderNo, String paymentKey, Long amount, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            log.warn("토스 서킷 브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        }
        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
