package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentRecord;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 결제 쓰기 담당 서비스
 * <p>
 * 결제의 상태변경, 생성과 같은 도메인 쓰기 로직을 담당
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 02.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 결제 생성
     * <p>
     * PG 승인 전 결제를 생성 메서드
     * 결제의 상태는 READY로 초기화 됨
     * </p>
     *
     * @param context 결제 생성 커맨드
     * @return 생성된 결제 결과
     */
    public PaymentResult.Default create(PaymentContext.Create context) {
        Payment payment = Payment.create(context.orderNo(), context.userId(), context.paymentKey(), context.totalAmount());
        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResult.Default.from(savedPayment);
    }

    /**
     * 결제 승인 성공
     * <p>
     *     결제 승인 상태로 변경하고 PaymentRecord를 저장한다
     * </p>
     * @param context 결제 승인 command
     * @return 결제 승인 처리 결과
     */
    public PaymentResult.PaymentApproval approve(PaymentContext.Approval context) {
        Payment payment = paymentRepository.findById(context.paymentId())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        PaymentRecord paymentRecord = PaymentRecord.createApproval(context.transactionKey(), context.amount(), context.approvedAt());
        payment.approve(paymentRecord, context.status(), context.method());
        PaymentCompleteEvent event = PaymentCompleteEvent.of(payment.getOrderNo(), payment.getId());
        eventPublisher.publishEvent(event);
        return PaymentResult.PaymentApproval.of(payment, paymentRecord);
    }

    /**
     * 결제 승인 실패
     * <p>
     *     결제 실패 상태로 변경한다
     * </p>
     * @param id 결제 아이디
     * @param failureCode 실패 코드
     */
    public void abort(Long id, String failureCode) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.abort(failureCode);
    }

    /**
     * 결제를 환불 대기 상태로 변경
     *
     * @param id 결제 아이디
     */
    public void changeRefundPending(Long id, LocalDateTime refundPendingAt) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.refundPending(refundPendingAt);
    }

    /**
     * 결제 취소 상태로 변경
     *
     * @param context 결제 취소 컨텍스트
     * @return 결제 취소 정보
     */
    public PaymentResult.PaymentCancel cancel(PaymentContext.Cancellation context) {
        Payment payment = paymentRepository.findById(context.paymentId())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        PaymentRecord paymentRecord = PaymentRecord.createCancellation(context.transactionKey(), context.amount(),
                context.cancelReason(), context.canceledAt());
        payment.cancel(paymentRecord, context.status());
        return PaymentResult.PaymentCancel.of(payment, paymentRecord);
    }
}
