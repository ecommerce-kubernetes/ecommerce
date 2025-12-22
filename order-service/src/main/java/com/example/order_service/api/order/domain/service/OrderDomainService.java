package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
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
@Transactional
public class OrderDomainService {
    private final OrderRepository orderRepository;

    public OrderCreationResult saveOrder(OrderCreationContext context){
        Order order = Order.create(context);
        Order savedOrder = orderRepository.save(order);
        return OrderCreationResult.from(savedOrder);
    }

    public void changePaymentWaiting(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        order.changeStatus(OrderStatus.PAYMENT_WAITING);
    }

    public void changeCanceled(Long orderId, OrderFailureCode code){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다"));
        order.canceled(code);
    }
}
