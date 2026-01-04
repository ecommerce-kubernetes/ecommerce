package com.example.order_service.api.order.infrastructure.client.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentCancelRequest {
    private String cancelReason;
    private Long cancelAmount;

    @Builder
    private TossPaymentCancelRequest(String cancelReason, Long cancelAmount) {
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
    }
}
