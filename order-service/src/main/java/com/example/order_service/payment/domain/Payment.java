package com.example.order_service.payment.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.payment.domain.context.ConfirmPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
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

    @Embedded
    private PaymentFailure failure;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

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

        PaymentStatus status = context.totalAmount().equals(Money.ZERO)
                ? PaymentStatus.DONE
                : PaymentStatus.READY;

        return Payment.builder()
                .id(id)
                .orderId(context.orderId())
                .userId(context.userId())
                .status(status)
                .totalAmount(context.totalAmount())
                .build();
    }

    public void approvePending(Money amount) {
        if (!this.status.equals(PaymentStatus.READY)) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_NOT_READY);
        }
        if (!this.totalAmount.equals(amount)) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        this.status = PaymentStatus.APPROVAL_PENDING;
    }

    public void confirm(ConfirmPaymentContext context, IdGenerator idGenerator) {

    }
}
