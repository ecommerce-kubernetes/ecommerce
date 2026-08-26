package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Optional<PaymentResult> findCompletedPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.DONE)
                .map(PaymentResult::from);
    }

    public List<PaymentResult> getPaymentsByReadyAndCreatedAtBefore(LocalDateTime threshold) {
        List<Payment> payments = paymentRepository.findPaymentsByStatusAndCreatedAtBefore(PaymentStatus.READY, threshold);
        return payments.stream().map(PaymentResult::from).toList();
    }

    public List<PaymentResult> getPaymentsByApprovePendingAndUpdatedAtBefore(LocalDateTime threshold) {
        List<Payment> payments = paymentRepository.findPaymentsByStatusAndUpdatedAtBefore(PaymentStatus.APPROVAL_PENDING, threshold);
        return payments.stream().map(PaymentResult::from).toList();
    }

    public List<PaymentResult> getPaymentsByRefundPendingAndUpdatedAtBefore(LocalDateTime threshold) {
        List<Payment> payments = paymentRepository.findPaymentsByStatusAndUpdatedAtBefore(PaymentStatus.REFUND_PENDING, threshold);
        return payments.stream().map(PaymentResult::from).toList();
    }
}
