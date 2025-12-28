package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.application.event.OrderEventCode;
import com.example.order_service.api.order.application.event.OrderEventStatus;
import com.example.order_service.api.order.application.event.PaymentResultEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaPaymentCommand {
    private Long orderId;
    private OrderEventStatus status;
    private OrderEventCode code;
    private String failureReason;

    @Builder
    private SagaPaymentCommand(Long orderId, OrderEventStatus status, OrderEventCode code, String failureReason) {
        this.orderId = orderId;
        this.status = status;
        this.code = code;
        this.failureReason = failureReason;
    }

    public static SagaPaymentCommand from(PaymentResultEvent event) {
        return SagaPaymentCommand.builder()
                .orderId(event.getOrderId())
                .status(event.getStatus())
                .code(event.getCode())
                .failureReason(event.getFailureReason())
                .build();
    }
}
