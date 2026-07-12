package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    private Long userId;
    private String paymentKey;
    private Money totalAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private PaymentMethod method;
    private String lastTransactionKey;
    private String failureCode;
    @Enumerated(EnumType.STRING)
    private PaymentManualCheckReason manualCheckReason;
    private LocalDateTime refundPendingAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String orderNo, Long userId, String paymentKey, Money totalAmount, PaymentStatus status, PaymentMethod method,
                    String lastTransactionKey, String failureCode, PaymentManualCheckReason manualCheckReason, LocalDateTime refundPendingAt) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.status = status;
        this.method = method;
        this.lastTransactionKey = lastTransactionKey;
        this.failureCode = failureCode;
        this.manualCheckReason = manualCheckReason;
        this.refundPendingAt = refundPendingAt;
    }

    public static Payment create(String orderNo, Long userId, String paymentKey, Money totalAmount) {
        return Payment.builder()
                .orderNo(orderNo)
                .userId(userId)
                .paymentKey(paymentKey)
                .totalAmount(totalAmount)
                .status(PaymentStatus.READY)
                .build();
    }

    public void approve(PaymentRecord approvalRecord, PaymentStatus status, PaymentMethod method) {
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
        }
        if (method == PaymentMethod.VIRTUAL_ACCOUNT) {
            throw new BusinessException(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD);
        }
        if (!this.totalAmount.equals(approvalRecord.getAmount())) {
            throw new BusinessException(PaymentErrorCode.PG_APPROVAL_AMOUNT_MISMATCH);
        }
        this.addRecord(approvalRecord);
        this.status = status;
        this.method = method;
    }

    public void abort(String failureCode) {
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_FAIL);
        }
        this.status = PaymentStatus.ABORTED;
        this.failureCode = failureCode;
    }

    public void refundPending(LocalDateTime refundPendingAt) {
        if (this.status != PaymentStatus.DONE) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
        }
        this.status = PaymentStatus.REFUND_PENDING;
        this.refundPendingAt = refundPendingAt;
    }

    public void manualChecking(PaymentManualCheckReason reason) {
        this.status = PaymentStatus.MANUAL_CHECK;
        this.manualCheckReason = reason;
    }

    public void cancel(PaymentRecord cancelledRecord, PaymentStatus status) {
        if (this.status != PaymentStatus.REFUND_PENDING) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND);
        }

        Money remainingAmount = calculateRemainingAmount();
        if (cancelledRecord.getAmount().isGreaterThan(remainingAmount)) {
            throw new BusinessException(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
        }

        this.addRecord(cancelledRecord);
        this.status = status;
    }

    public Money calculateTotalCanceledAmount() {
        return this.paymentRecords.stream()
                .filter(record -> record.getType() == TransactionType.REFUND)
                .map(PaymentRecord::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money calculateRemainingAmount() {
        return this.totalAmount.subtract(calculateTotalCanceledAmount());
    }

    private void addRecord(PaymentRecord paymentRecord) {
        this.paymentRecords.add(paymentRecord);
        paymentRecord.setPayment(this);
        this.lastTransactionKey = paymentRecord.getTransactionKey();
    }
}
