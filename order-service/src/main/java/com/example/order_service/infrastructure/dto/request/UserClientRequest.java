package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

public class UserClientRequest {

    @Builder
    public record ValidatePoints(
            Long usedPoints
    ) {
        public static ValidatePoints of(Long usedPoints) {
            return ValidatePoints.builder()
                    .usedPoints(usedPoints)
                    .build();
        }
    }
}
