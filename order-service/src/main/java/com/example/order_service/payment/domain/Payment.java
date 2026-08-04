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
import org.springframework.util.Assert;

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

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    private String paymentKey;

    private Money totalAmount;

    private PaymentFailure failure;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(Long id, Long orderId, Long userId, PaymentStatus status, PaymentMethod method, PaymentProvider provider, String paymentKey, Money totalAmount, PaymentFailure failure) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.method = method;
        this.provider = provider;
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.failure = failure;
    }

    public static Payment create(CreatePaymentContext context, IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "결제 생성시 아이디 생성기는 필수이다.");
        Long id = idGenerator.generate();
        Assert.notNull(id, "결제 생성시 아이디는 필수이다.");
        Assert.notNull(context.orderId(), "결제 생성시 주문 아이디는 필수이다.");
        Assert.notNull(context.userId(), "결제 생성시 유저 아이디는 필수이다.");
        Assert.notNull(context.totalAmount(), "결제 생성시 결제 금액은 필수이다.");

        return Payment.builder()
                .id(id)
                .orderId(context.orderId())
                .userId(context.userId())
                .status(PaymentStatus.APPROVAL_PENDING)
                .totalAmount(context.totalAmount())
                .build();
    }

}
