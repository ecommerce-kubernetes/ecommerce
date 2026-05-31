package com.example.order_service.order.application.dto.result;

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

}
