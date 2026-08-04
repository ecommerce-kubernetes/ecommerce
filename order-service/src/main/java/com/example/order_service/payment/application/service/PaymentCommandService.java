package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.Payment;
import com.example.order_service.payment.domain.PaymentManualCheckReason;
import com.example.order_service.payment.domain.PaymentRecord;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentResult.Default create(PaymentContext.Create context) {
        return null;
    }

    public PaymentResult.PaymentApproval approve(PaymentContext.Approval context) {
        return null;
    }

    public void abort(Long id, String failureCode) {

    }

    public void changeRefundPending(Long id, LocalDateTime refundPendingAt) {

    }

    public PaymentResult.PaymentCancel cancel(PaymentContext.Cancellation context) {
        return null;
    }

    public void changeApprovalManualCheck(Long id) {

    }

    public void changeRefundManualCheck(Long id) {

    }
}
