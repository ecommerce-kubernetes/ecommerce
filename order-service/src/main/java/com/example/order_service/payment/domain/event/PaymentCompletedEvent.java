package com.example.order_service.payment.domain.event;

import lombok.Builder;
import org.springframework.util.Assert;

@Builder
public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        Long userId
) {
    public PaymentCompletedEvent {
        Assert.notNull(paymentId, "결제 승인 이벤트의 결제 아이디는 필수이다.");
        Assert.notNull(orderId, "결제 승인 이벤트의 주문 아이디는 필수이다.");
        Assert.notNull(userId, "결제 승인 이벤트의 유저 아이디는 필수이다.");
    }

    public static PaymentCompletedEvent of(Long paymentId, Long orderId, Long userId) {
        return PaymentCompletedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .build();
    }
}
