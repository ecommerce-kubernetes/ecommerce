package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

public class UserClientRequest {

    @Builder
    public record ValidatePoints(
            Long orderAmount,
            Long usedPoints
    ) {
    }
}
