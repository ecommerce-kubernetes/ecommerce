package com.example.order_service.infrastructure.dto.response.user;

import lombok.Builder;

@Builder
public record UserPointsResponse(
        Long userId,
        Long availablePoints
) {
}
