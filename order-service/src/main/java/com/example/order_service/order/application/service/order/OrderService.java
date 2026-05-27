package com.example.order_service.order.application.service.order;

import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    public OrderDto saveOrder(OrderContext.CreateOrderContext context) {
        return null;
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderNo, Long userId) {
        return null;
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(Long userId, OrderSearchCondition condition) {
        return null;
    }
}
