package com.example.order_service.order.infrastructure.messaging.dto;

import com.example.order_service.order.domain.saga.SagaStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SagaReplyMessage {
    private SagaResult result;
    private String orderNo;
    private SagaStep step;
    private String code;
}
