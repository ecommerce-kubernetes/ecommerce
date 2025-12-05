package com.example.order_service.api.order.controller.dto.response;

import com.example.order_service.dto.response.OrderPaymentSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class OrderDetailResponse {
    private Long orderId;
    private String status;
    private LocalDateTime createAt;
    private String deliveryAddress;
    private OrderPaymentSummary orderPaymentSummary;
    private List<OrderItemResponse> orderItems;
}
