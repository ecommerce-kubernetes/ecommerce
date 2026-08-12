package com.example.order_service.order.infrastructure.messaging.dto;

import com.example.order_service.saga.domain.tmp.SagaStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SagaReplyMessage {
    private SagaResult result;
    private Long sagaId;
    private String orderNo;
    private SagaStep step;
    private String code;
}
