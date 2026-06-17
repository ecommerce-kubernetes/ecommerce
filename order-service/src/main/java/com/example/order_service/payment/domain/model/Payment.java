package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    private Long userId;
    private String paymentKey;
    private Money totalAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String orderNo, Long userId, String paymentKey, Money totalAmount, PaymentStatus status) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.status = status;
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

    public void approval(PaymentRecord approvalRecord, PaymentStatus status) {
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
        }
        this.addRecord(approvalRecord);
        this.status = status;
    }

    public void done(PaymentRecord approvalRecord) {
        if (this.status != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
        }
        this.addRecord(approvalRecord);
        this.status = PaymentStatus.DONE;
    }

    public void fail(String reason) {

    }

    public void refundPending() {
        if (this.status != PaymentStatus.DONE) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
        }
        this.status = PaymentStatus.REFUND_PENDING;
    }

    public void cancel(PaymentRecord cancelledRecord, PaymentStatus status) {
        if (this.status != PaymentStatus.REFUND_PENDING) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND);
        }

        Money remainingAmount = calculateRemainingAmount();
        if (cancelledRecord.getAmount().isGreaterThan(remainingAmount)){
            throw new BusinessException(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
        }

        this.addRecord(cancelledRecord);
        this.status = status;
    }

    public Money calculateTotalCanceledAmount(){
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
    }
}
