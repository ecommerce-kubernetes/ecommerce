package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
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
            throw new UnavailableServiceException("토스 페이먼츠 서비스가 응답하지 않습니다");
        }

        if (throwable instanceof PaymentException) {
            throw (PaymentException) throwable;
        }
        throw new InternalServerException("토스 페이먼츠 서비스에서 오류가 발생했습니다");
    }
}
