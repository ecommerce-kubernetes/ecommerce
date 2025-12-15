package com.example.order_service.api.order.saga.domain.service.dto;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SagaInstanceDto {
    private Long id;
    private Long orderId;
    private String step;
    private String progress;
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
