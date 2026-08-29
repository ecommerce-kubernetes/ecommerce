package com.example.userservice.user.application.service.dto.result;

import lombok.Builder;

@Builder
public record UserPointsResult(
        Long userId,
        Long availablePoints
) {
}
