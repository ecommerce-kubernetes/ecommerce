package com.example.order_service.order.application;

import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.domain.service.dto.command.PaymentCreationContext;
import org.springframework.stereotype.Component;

@Component
public class OrderCreationContextMapper {

    public PaymentCreationContext mapPaymentCreationContext(OrderPaymentResult.Payment orderPaymentInfo) {
        return PaymentCreationContext.builder()
                .orderNo(orderPaymentInfo.orderNo())
                .paymentKey(orderPaymentInfo.paymentKey())
                .amount(orderPaymentInfo.totalAmount())
                .status(orderPaymentInfo.status())
                .method(orderPaymentInfo.method())
                .approvedAt(orderPaymentInfo.approvedAt())
                .build();
    }
}
