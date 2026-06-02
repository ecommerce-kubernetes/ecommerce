package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentRecord;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentResult.Default save(PaymentContext.Create context) {
        Payment payment = createPayment(context);
        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResult.Default.from(savedPayment);
    }

    private Payment createPayment(PaymentContext.Create context) {
        return Payment.create(context.orderNo(), context.userId(), context.paymentKey(), context.totalAmount());
    }

    public PaymentResult.PaymentApproval approve(PaymentContext.Approval context) {
        Payment payment = paymentRepository.findById(context.paymentId())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        PaymentRecord paymentRecord = createApprovalPaymentRecord(context);
        payment.addRecord(paymentRecord);
        payment.changeStatus(context.status());

        if (payment.getStatus() == PaymentStatus.DONE) {
            PaymentCompleteEvent event = PaymentCompleteEvent.of(payment.getOrderNo(),
                    payment.getPaymentKey());
            eventPublisher.publishEvent(event);
        }
        return PaymentResult.PaymentApproval.of(payment, paymentRecord);
    }

    private PaymentRecord createApprovalPaymentRecord(PaymentContext.Approval context) {
        return PaymentRecord.createApproval(context.amount(), context.method(), context.approvedAt());
    }
}
