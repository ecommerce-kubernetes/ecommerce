package com.example.order_service.api.order.facade.dto;

import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderPreparationData {
    private OrderUserResponse user;
    private List<OrderProductResponse> products;
}
