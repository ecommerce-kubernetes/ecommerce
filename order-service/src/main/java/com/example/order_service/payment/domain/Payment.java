package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    private Long id;

    private Long orderId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String paymentKey;

    private Money totalAmount;

    private PaymentFailure failure;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Payment (Long id, Long orderId, PaymentStatus status, PaymentMethod method, String paymentKey, Money totalAmount, PaymentFailure failure) {

    }
    public static Payment create(CreatePaymentContext context, IdGenerator idGenerator) {
        return null;
    }

}
