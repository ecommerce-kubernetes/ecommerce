package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    private String transactionKey;
    private TransactionType type;
    private Money amount;
    private String reason;
    private LocalDateTime occurredAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentRecord(String transactionKey, TransactionType type, Money amount, String reason, LocalDateTime occurredAt) {
        this.transactionKey = transactionKey;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public static PaymentRecord createApproval(String transactionKey, Money amount, LocalDateTime occurredAt) {
        return PaymentRecord.builder()
                .transactionKey(transactionKey)
                .type(TransactionType.PAYMENT)
                .amount(amount)
                .reason("정상 승인")
                .occurredAt(occurredAt)
                .build();
    }

    public static PaymentRecord createCancellation(String transactionKey, Money amount, String reason, LocalDateTime occurredAt) {
        return PaymentRecord.builder()
                .type(TransactionType.REFUND)
                .amount(amount)
                .reason(reason)
                .occurredAt(occurredAt)
                .build();
    }

    protected void setPayment(Payment payment) {
        this.payment = payment;
    }
}
