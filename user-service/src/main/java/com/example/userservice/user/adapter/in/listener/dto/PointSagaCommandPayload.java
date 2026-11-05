package com.example.userservice.user.adapter.in.listener.dto;

import lombok.Builder;

@Builder
public record PointSagaCommandPayload(
        Long executionId,
        Long userId,
        Long usedPoints
) {
}
