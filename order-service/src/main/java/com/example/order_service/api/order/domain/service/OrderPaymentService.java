package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.model.PaymentMethod;
import com.example.order_service.api.order.domain.model.PaymentStatus;
import com.example.order_service.api.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
    private final TossPaymentAdaptor tossPaymentAdaptor;

    public OrderPaymentInfo confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        TossPaymentConfirmResponse paymentResponse = tossPaymentAdaptor.confirmPayment(orderNo, paymentKey, amount);
        return mapToOrderPaymentInfo(paymentResponse);
    }

    public void cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        tossPaymentAdaptor.cancelPayment(paymentKey, cancelReason, cancelAmount);
    }

    private OrderPaymentInfo mapToOrderPaymentInfo(TossPaymentConfirmResponse response) {
        return OrderPaymentInfo.builder()
                .orderNo(response.getOrderId())
                .paymentKey(response.getPaymentKey())
                .totalAmount(response.getTotalAmount())
                .status(PaymentStatus.from(response.getStatus()))
                .method(PaymentMethod.from(response.getMethod()))
                .approvedAt(parseDateTime(response.getApprovedAt()))
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }
}
