package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String status;
    private String orderName;
    private Long finalPaymentAmount;
    private LocalDateTime createAt;

    @Builder
    private CreateOrderResponse(Long orderId, String status, String orderName, LocalDateTime createAt, Long finalPaymentAmount){
        this.orderId = orderId;
        this.status = status;
        this.orderName = orderName;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createAt = createAt;
    }

    public static CreateOrderResponse of(OrderCreationResult result) {
        return CreateOrderResponse.builder()
                .orderId(result.getOrderId())
                .status(result.getStatus())
                .orderName(result.getOrderName())
                .finalPaymentAmount(result.getPaymentInfo().getFinalPaymentAmount())
                .createAt(result.getOrderedAt())
                .build();
    }
}
