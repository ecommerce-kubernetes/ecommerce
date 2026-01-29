package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderListResponse {
    private String orderNo;
    private String orderStatus;
    private List<OrderItemResponse> orderItems;
    private String createdAt;

    public static OrderListResponse from(OrderDto orderDto){
        List<OrderItemResponse> orderItemResponses = orderDto.getOrderItems().stream().map(OrderItemResponse::from).toList();
        return OrderListResponse.builder()
                .orderNo(orderDto.getOrderNo())
                .orderStatus(orderDto.getStatus().name())
                .orderItems(orderItemResponses)
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}
