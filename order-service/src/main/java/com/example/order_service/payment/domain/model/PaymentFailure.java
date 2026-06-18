package com.example.order_service.payment.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentFailure {

    @Id
    private Long paymentId;
    private String reason;
    private String errorCode;
    private LocalDateTime failedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PaymentFailure(Long paymentId, String reason, String errorCode, LocalDateTime failedAt) {
        this.paymentId = paymentId;
        this.reason = reason;
        this.errorCode = errorCode;
        this.failedAt = failedAt;
    }
}
