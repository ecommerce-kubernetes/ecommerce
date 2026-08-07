package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import org.springframework.stereotype.Component;

@Component
public class PaymentContextFactory {

    public CreatePaymentContext create(Long orderId, Long userId, Money totalAmount) {
        return CreatePaymentContext.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .build();
    }
}
