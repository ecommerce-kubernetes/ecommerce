package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

}
