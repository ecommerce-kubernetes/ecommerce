package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmRequest;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TossPaymentClientService {
    private final TossPaymentClient tossPaymentClient;

    @CircuitBreaker(name = "tossPaymentService")
    public TossPaymentConfirmResponse confirmPayment(Long orderId, String paymentKey, Long amount){
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(orderId, paymentKey, amount);
        return tossPaymentClient.confirmPayment(request);
    }
}
