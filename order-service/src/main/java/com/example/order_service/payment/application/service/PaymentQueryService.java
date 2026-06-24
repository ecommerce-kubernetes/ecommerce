package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.domain.repository.PaymentQueryRepository;
import com.example.order_service.payment.domain.repository.PaymentRepository;
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
    private final PaymentQueryRepository paymentQueryRepository;
    /**
     * 결제 조회
     *
     * @param id 결제 아이디
     * @return 결제 정보
     */
    public PaymentResult.Default getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResult.Default.from(payment);
    }

    public List<PaymentResult.Default> getReadyPaymentsBefore(LocalDateTime threshold, int size) {
        List<Payment> payments = paymentQueryRepository.findReadyPaymentsBefore(threshold, size);
        return payments.stream().map(PaymentResult.Default::from).toList();
    }

    public List<PaymentResult.Default> getRefundPendingPaymentsBefore(LocalDateTime threshold, int size) {
        List<Payment> payments = paymentQueryRepository.findRefundPendingPaymentsBefore(threshold, size);
        return payments.stream().map(PaymentResult.Default::from).toList();
    }
}
