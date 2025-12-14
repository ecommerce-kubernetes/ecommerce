package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderDomainService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderCreationResult saveOrder(OrderCreationContext context){
        Order order = Order.create(context);
        Order savedOrder = orderRepository.save(order);
        return OrderCreationResult.from(savedOrder);
    }

}
