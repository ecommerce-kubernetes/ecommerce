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
    private String method;
    private LocalDateTime approvedAt;

    protected void setOrder(Order order) {
        this.order = order;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(Long amount, String paymentKey, String method, LocalDateTime approvedAt) {
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.method = method;
        this.approvedAt = approvedAt;
    }

    public static Payment create(Long amount, String paymentKey, String method, LocalDateTime approvedAt) {
        return Payment.builder()
                .amount(amount)
                .paymentKey(paymentKey)
                .method(method)
                .approvedAt(approvedAt)
                .build();
    }
}
