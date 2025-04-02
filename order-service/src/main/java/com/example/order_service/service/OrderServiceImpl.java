package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    @Override
    public OrderResponseDto saveOrder(Long userId, OrderRequestDto orderRequestDto) {
        return null;
    }
}
