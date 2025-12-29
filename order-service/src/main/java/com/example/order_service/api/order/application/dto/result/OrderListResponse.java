package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
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
    private String createdAt;

    @Builder
    private OrderListResponse(Long orderId, Long userId, String orderStatus, List<OrderItemResponse> orderItems, String createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }

    public static OrderListResponse from(OrderDto orderDto){
        List<OrderItemResponse> orderItemResponses = orderDto.getOrderItemDtoList().stream().map(OrderItemResponse::from).toList();
        return OrderListResponse.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .orderStatus(orderDto.getStatus().name())
                .orderItems(orderItemResponses)
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}
