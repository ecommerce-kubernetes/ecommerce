package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

public class TossClientRequest {

    @Builder
    public record Confirm(
            String orderId,
            String paymentKey,
            Long amount
    ) {
        public static Confirm of(String orderId, String paymentKey, Long amount) {
            return Confirm.builder()
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .amount(amount)
                    .build();
        }
    }

    @Builder
    public record Cancel(
            String cancelReason,
            Long cancelAmount
    ) {
        public static Cancel of(String cancelReason, Long cancelAmount) {
            return Cancel.builder()
                    .cancelReason(cancelReason)
                    .cancelAmount(cancelAmount)
                    .build();
        }
    }

}
