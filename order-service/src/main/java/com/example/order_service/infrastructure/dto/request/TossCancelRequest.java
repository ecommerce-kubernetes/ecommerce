package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

@Builder
public record TossCancelRequest(
        String cancelReason,
        Long cancelAmount
) {
    public static TossCancelRequest of(String cancelReason, Long cancelAmount) {
        return TossCancelRequest.builder()
                .cancelReason(cancelReason)
                .cancelAmount(cancelAmount)
                .build();
    }
}
