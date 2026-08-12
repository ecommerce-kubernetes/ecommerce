package com.example.order_service.order.infrastructure.messaging.dto;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.saga.domain.tmp.SagaStep;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointsMessage {
    private SagaType type;
    private String orderNo;
    private SagaStep step;
    private Points points;

    @Builder
    public record Points(
            Long userId,
            Long usedPoints
    ) {
        public static Points from(SagaPayload payload) {
            return Points.builder()
                    .userId(payload.getUserId())
                    .usedPoints(payload.getPoints().getUsedPoints().longValue())
                    .build();
        }
    }

    public static PointsMessage deduct(SagaMessage message) {
        return PointsMessage.builder()
                .type(SagaType.DEDUCT_POINTS)
                .orderNo(message.getOrderNo())
                .step(message.getStep())
                .points(Points.from(message.getPayload()))
                .build();
    }

    public static PointsMessage restore(SagaMessage message) {
        return PointsMessage.builder()
                .type(SagaType.RESTORE_POINTS)
                .orderNo(message.getOrderNo())
                .step(message.getStep())
                .points(Points.from(message.getPayload()))
                .build();
    }
}
