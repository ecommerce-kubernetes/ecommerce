package com.example.order_service.api.order.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderListResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;

    @Builder
    private OrderListResponse(Long orderId, Long userId, String orderStatus, List<OrderItemResponse> orderItems, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }
}
