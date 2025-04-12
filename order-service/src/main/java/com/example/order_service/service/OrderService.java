package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.dto.response.PageDto;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto saveOrder(Long userId, OrderRequestDto orderRequestDto);
    PageDto<OrderResponseDto> getOrderList(Pageable pageable, Long userId, Integer year, String keyword);
}
