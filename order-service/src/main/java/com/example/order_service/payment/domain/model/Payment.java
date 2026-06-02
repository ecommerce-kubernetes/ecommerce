package com.example.order_service.payment.domain.model;

import com.example.order_service.common.domain.vo.Money;
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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    private Long userId;
    private String paymentKey;
    private Money totalAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String orderNo, Long userId, String paymentKey, Money totalAmount, PaymentStatus status) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public static Payment create(String orderNo, Long userId, String paymentKey, Money totalAmount) {
        return Payment.builder()
                .orderNo(orderNo)
                .userId(userId)
                .paymentKey(paymentKey)
                .totalAmount(totalAmount)
                .status(PaymentStatus.READY)
                .build();
    }

    public void addRecord(PaymentRecord paymentRecord) {
        this.paymentRecords.add(paymentRecord);
        paymentRecord.setPayment(this);
    }

    public void changeStatus(PaymentStatus paymentStatus) {
        this.status = paymentStatus;
    }

}
