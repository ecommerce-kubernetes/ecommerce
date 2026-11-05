package com.example.order_service.saga.adapter.out.message.processor.dto;

import com.example.order_service.saga.domain.event.UsedPointEvent;
import lombok.Builder;

@Builder
public record PointMessagePayload(
        Long executionId,
        Long userId,
        Long usedPoints
) {

    public static PointMessagePayload from(UsedPointEvent event) {
        return PointMessagePayload.builder()
                .executionId(event.executionId())
                .userId(event.userId())
                .usedPoints(event.usedPoints().longValue())
                .build();
    }
}
