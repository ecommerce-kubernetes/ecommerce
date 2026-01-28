package com.example.order_service.api.order.domain.model;

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
    private PaymentType type;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    private LocalDateTime approvedAt;

    protected void setOrder(Order order) {
        this.order = order;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String paymentKey, Long amount, PaymentType type, PaymentStatus status, PaymentMethod method, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.method = method;
        this.approvedAt = approvedAt;
    }
}
