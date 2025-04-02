package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderResponseDto;

public interface OrderService {

    OrderResponseDto saveOrder(Long userId, OrderRequestDto orderRequestDto);
}
