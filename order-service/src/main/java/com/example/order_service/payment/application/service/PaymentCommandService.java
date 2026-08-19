package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.application.port.PaymentRepository;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final IdGenerator idGenerator;

    public Long create(CreatePaymentContext context) {
        Payment payment = Payment.create(context, idGenerator);
        Payment save = paymentRepository.save(payment);
        return save.getId();
    }

    public void approvePending(Long paymentId, Long userId, ApprovePendingPaymentContext context) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.approvePending(context);
    }

    public void approve(Long paymentId, Long userId, ApprovePaymentContext context) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.approve(context, idGenerator);
        paymentRepository.save(payment);
    }

    public void abort(Long paymentId, Long userId, PaymentFailure failure) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.abort(failure);
    }

    public void refundPending(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.refundPending();
    }

    public void cancel(Long paymentId, Long userId, CancelPaymentContext context) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.cancel(context, idGenerator);
    }
}
