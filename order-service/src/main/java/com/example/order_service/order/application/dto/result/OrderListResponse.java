package com.example.order_service.order.application.dto.result;

import com.example.order_service.order.application.service.order.dto.result.OrderDto;
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
        return null;
    }
}
