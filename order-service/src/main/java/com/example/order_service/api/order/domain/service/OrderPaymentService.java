package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
    private final TossPaymentAdaptor tossPaymentAdaptor;

    public OrderPaymentInfo confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        TossPaymentConfirmResponse paymentResponse = tossPaymentAdaptor.confirmPayment(orderNo, paymentKey, amount);
        return null;
    }

    public void cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        tossPaymentAdaptor.cancelPayment(paymentKey, cancelReason, cancelAmount);
    }

    public OrderPaymentInfo mapToOrderPaymentInfo(TossPaymentConfirmResponse response) {
        return OrderPaymentInfo.builder()
                .orderNo(response.getOrderId())
                .paymentKey(response.getPaymentKey())
                .totalAmount(response.getTotalAmount())
                .status(response.getStatus())
                .method(response.getMethod())
                .approvedAt(response.getApprovedAt())
                .build();
    }
}
