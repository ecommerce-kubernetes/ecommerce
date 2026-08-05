package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    public PaymentResult.Default getPayment(Long id) {
        return null;
    }

    public List<PaymentResult.Default> getReadyPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }

    public List<PaymentResult.Default> getRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }
}
