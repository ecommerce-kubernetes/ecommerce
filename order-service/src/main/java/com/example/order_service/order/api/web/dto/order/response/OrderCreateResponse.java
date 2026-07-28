package com.example.order_service.order.api.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import lombok.Builder;

@Builder
public record OrderCreateResponse(
        Long orderId
) {
    public static OrderCreateResponse from(OrderResult.Create result) {
        return OrderCreateResponse.builder()
                .orderId(result.orderId())
                .build();
    }
}
