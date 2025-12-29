package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String status;
    private String orderName;
    private Long finalPaymentAmount;
    private String createdAt;

    @Builder
    private CreateOrderResponse(Long orderId, String status, String orderName, String createdAt, Long finalPaymentAmount){
        this.orderId = orderId;
        this.status = status;
        this.orderName = orderName;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createdAt = createdAt;
    }

    public static CreateOrderResponse of(OrderDto result) {
        return CreateOrderResponse.builder()
                .orderId(result.getOrderId())
                .status(result.getStatus().name())
                .orderName(result.getOrderName())
                .finalPaymentAmount(result.getPaymentInfo().getFinalPaymentAmount())
                .createdAt(result.getOrderedAt().toString())
                .build();
    }
}
