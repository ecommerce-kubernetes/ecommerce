package com.example.order_service.payment.application.port;

import com.example.order_service.payment.application.port.dto.PaymentOrderResult;

public interface PaymentOrderPort {
    PaymentOrderResult getOrder(Long orderId, Long userId);
}
