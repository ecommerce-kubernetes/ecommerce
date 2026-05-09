package com.example.order_service.order.api.dto.response;

import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

public class OrderResponse {

    @Builder
    public record Create(
            String orderNo,
            OrderStatus status,
            String orderName,
            Long finalPaymentAmount,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime createdAt
    ) {
        public static Create from(OrderResult.Create result) {
            return Create.builder()
                    .orderNo(result.orderNo())
                    .status(result.status())
                    .orderName(result.orderName())
                    .finalPaymentAmount(result.finalPaymentAmount().longValue())
                    .createdAt(result.createdAt())
                    .build();
        }
    }
}
