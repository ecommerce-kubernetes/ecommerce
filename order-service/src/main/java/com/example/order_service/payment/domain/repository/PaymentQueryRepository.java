package com.example.order_service.payment.domain.repository;

import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentQueryRepository {
    List<Payment> findReadyPaymentsBefore(LocalDateTime threshold, int size);
    List<Payment> findRefundPendingPaymentsBefore(LocalDateTime threshold, int size);
}
