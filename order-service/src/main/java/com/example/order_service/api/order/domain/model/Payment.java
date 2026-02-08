package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    private String paymentKey;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    private LocalDateTime approvedAt;

    protected void setOrder(Order order) {
        this.order = order;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String paymentKey, Long amount, PaymentStatus status, PaymentMethod method, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static Payment create(PaymentCreationContext context) {
        return Payment.builder()
                .paymentKey(context.getPaymentKey())
                .amount(context.getAmount())
                .status(context.getStatus())
                .method(context.getMethod())
                .approvedAt(context.getApprovedAt())
                .build();
    }
}
