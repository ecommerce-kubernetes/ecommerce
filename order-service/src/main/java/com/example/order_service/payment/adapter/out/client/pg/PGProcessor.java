package com.example.order_service.payment.adapter.out.client.pg;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.domain.PaymentProvider;

public interface PGProcessor {
    PaymentProvider getSupportedProvider();
    PGConfirmResult confirm(Long orderId, String paymentKey, Money amount);
    void netCancel(String paymentKey, String cancelReason);
    PGCancelResult cancel(String paymentKey, String cancelReason);
}
