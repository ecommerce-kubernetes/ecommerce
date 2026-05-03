package com.example.order_service.order.application.dto.result;

import lombok.Builder;

public class OrderUserResult {

    @Builder
    public record OrdererInfo(
            Long userId,
            Long availablePoints,
            String ordererName,
            String ordererPhone
    ) {
    }
}
