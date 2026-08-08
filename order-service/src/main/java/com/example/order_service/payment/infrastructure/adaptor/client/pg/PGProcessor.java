package com.example.order_service.payment.infrastructure.adaptor.client.pg;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;

public interface PGProcessor {
    PaymentProvider getSupportedProvider();
    PGConfirmResult confirm(Long orderId, String paymentKey, Money amount);
}
