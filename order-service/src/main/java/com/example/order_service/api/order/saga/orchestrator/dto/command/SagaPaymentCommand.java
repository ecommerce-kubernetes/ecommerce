package com.example.order_service.api.order.saga.orchestrator.dto.command;

import com.example.order_service.api.order.application.event.OrderEventStatus;
import com.example.order_service.api.order.application.event.PaymentResultEvent;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SagaPaymentCommand {
    private String orderNo;
    private OrderEventStatus status;
    private OrderFailureCode code;
    private String failureReason;

    @Builder
    private SagaPaymentCommand(String orderNo, OrderEventStatus status, OrderFailureCode code, String failureReason) {
        this.orderNo = orderNo;
        this.status = status;
        this.code = code;
        this.failureReason = failureReason;
    }

    public static SagaPaymentCommand from(PaymentResultEvent event) {
        SagaPaymentCommandBuilder builder = SagaPaymentCommand.builder()
                .orderNo(event.getOrderNo())
                .status(event.getStatus());
        if (event.getCode() != null) {
            builder.code(event.getCode())
                    .failureReason(event.getCode().getName());
        }
        return builder.build();
    }
}
