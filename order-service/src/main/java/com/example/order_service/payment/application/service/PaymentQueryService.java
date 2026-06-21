package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;

    /**
     * 결제 조회
     *
     * @param orderNo 주문 번호
     * @return 결제 정보
     */
    public PaymentResult.Default getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResult.Default.from(payment);
    }
}
