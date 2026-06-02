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
    private TransactionType type;
    private Money amount;
    private PaymentMethod method;
    private LocalDateTime approvedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentRecord(TransactionType type, Money amount, PaymentMethod method, LocalDateTime approvedAt) {
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static PaymentRecord createApproval(Money amount, PaymentMethod method, LocalDateTime approvedAt) {
        return PaymentRecord.builder()
                .type(TransactionType.PAYMENT)
                .amount(amount)
                .method(method)
                .approvedAt(approvedAt)
                .build();
    }

    protected void setPayment(Payment payment) {
        this.payment = payment;
    }
}
