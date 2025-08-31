package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse saveOrder(Long userId, OrderRequest orderRequest);
    PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword);
    void changeOrderStatus(Long orderId, String status);
}
