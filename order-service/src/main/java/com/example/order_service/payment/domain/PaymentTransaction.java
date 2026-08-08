package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTransaction extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String transactionKey;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Money amount;

    private String reason;

    private LocalDateTime occurredAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentTransaction(Long id, String transactionKey, TransactionType type, Money amount, String reason, LocalDateTime occurredAt) {
        this.id = id;
        this.transactionKey = transactionKey;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public static PaymentTransaction createApproval(String transactionKey, Money amount, LocalDateTime occurredAt, IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "결제 승인시 아이디 생성기는 필수이다.");
        Long id = idGenerator.generate();
        Assert.notNull(id, "결제 승인시 아이디는 필수이다.");

        return PaymentTransaction.builder()
                .id(id)
                .transactionKey(transactionKey)
                .type(TransactionType.PAYMENT)
                .amount(amount)
                .reason("정상 승인")
                .occurredAt(occurredAt)
                .build();
    }
}
