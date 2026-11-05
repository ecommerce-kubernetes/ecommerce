package com.example.order_service.order.adapter.in.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record OrderCreateResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderId
) {
    public static OrderCreateResponse from(OrderCreateResult result) {
        return OrderCreateResponse.builder()
                .orderId(result.orderId())
                .build();
    }
}
