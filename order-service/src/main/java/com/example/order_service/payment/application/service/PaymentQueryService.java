package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResultDeprecated;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentResult getPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResult.from(payment);
    }

    public PaymentResultDeprecated.Default getPayment(Long id) {
        return null;
    }

    public List<PaymentResultDeprecated.Default> getReadyPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }

    public List<PaymentResultDeprecated.Default> getRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        return null;
    }
}
