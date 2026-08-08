package com.example.order_service.infrastructure.gateway;

import com.example.order_service.payment.domain.PaymentProvider;

public interface PGGateway {
    PaymentProvider getSupportedProvider();
}
