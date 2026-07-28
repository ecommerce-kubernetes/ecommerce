package com.example.order_service.order.api.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import lombok.Builder;

@Builder
public record OrderCreateResponse(
        Long orderId
) {
    public static OrderCreateResponse from(OrderCreateResult result) {
        return OrderCreateResponse.builder()
                .orderId(result.orderId())
                .build();
    }
}
