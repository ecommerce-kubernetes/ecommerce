package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderDomainService {
    private final OrderRepository orderRepository;

    public OrderDto saveOrder(OrderCreationContext context){
        Order order = Order.create(context);
        Order savedOrder = orderRepository.save(order);
        return OrderDto.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        return OrderDto.from(order);
    }

    @Transactional
    public Page<OrderDto> getOrders(Long userId, OrderSearchCondition condition) {
        return null;
    }

    public OrderDto changeOrderStatus(Long orderId, OrderStatus orderStatus){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        order.changeStatus(orderStatus);
        return OrderDto.from(order);
    }

    public OrderDto changeCanceled(Long orderId, OrderFailureCode code){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        order.canceled(code);
        return OrderDto.from(order);
    }
}
