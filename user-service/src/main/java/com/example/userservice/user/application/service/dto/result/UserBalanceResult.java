package com.example.userservice.user.application.service.dto.result;

import lombok.Builder;

@Builder
public record UserBalanceResult(
        Long userId,
        Long availablePoints
) {
}
