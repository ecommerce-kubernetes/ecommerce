package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
    private final TossPaymentAdaptor tossPaymentAdaptor;

    public TossPaymentConfirmResponse confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        return tossPaymentAdaptor.confirmPayment(orderNo, paymentKey, amount);
    }

    public void cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        tossPaymentAdaptor.cancelPayment(paymentKey, cancelReason, cancelAmount);
    }

}
