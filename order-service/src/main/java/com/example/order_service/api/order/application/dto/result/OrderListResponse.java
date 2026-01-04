package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderListResponse {
    private String orderNo;
    private Long userId;
    private String orderStatus;
    private List<OrderItemResponse> orderItems;
    private String createdAt;

    @Builder
    private OrderListResponse(String orderNo, Long userId, String orderStatus, List<OrderItemResponse> orderItems, String createdAt) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }

    public static OrderListResponse from(OrderDto orderDto){
        List<OrderItemResponse> orderItemResponses = orderDto.getOrderItemDtoList().stream().map(OrderItemResponse::from).toList();
        return OrderListResponse.builder()
                .orderNo(orderDto.getOrderNo())
                .userId(orderDto.getUserId())
                .orderStatus(orderDto.getStatus().name())
                .orderItems(orderItemResponses)
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}
