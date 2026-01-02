package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.Payment;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Order order = getByOrderId(orderId);
        return OrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(Long userId, OrderSearchCondition condition) {
        Page<Order> orders = orderRepository.findByUserIdAndCondition(userId, condition);
        List<OrderDto> orderDtoList = orders.getContent().stream().map(OrderDto::from).toList();
        return new PageImpl<>(orderDtoList, orders.getPageable(), orders.getTotalElements());
    }

    public OrderDto changeOrderStatus(Long orderId, OrderStatus orderStatus){
        Order order = getByOrderId(orderId);
        order.changeStatus(orderStatus);
        return OrderDto.from(order);
    }

    public OrderDto canceledOrder(Long orderId, OrderFailureCode code){
        Order order = getByOrderId(orderId);
        order.canceled(code);
        return OrderDto.from(order);
    }

    @Transactional
    public OrderDto completedOrder(PaymentCreationCommand command) {
        Order order = getByOrderId(command.getOrderId());
        order.changeStatus(OrderStatus.COMPLETED);
        Payment payment = Payment.create(command.getAmount(), command.getPaymentKey(), command.getMethod(), command.getApprovedAt());
        order.addPayment(payment);
        Order savedOrder = orderRepository.saveAndFlush(order);
        return OrderDto.from(savedOrder);
    }

    private Order getByOrderId(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}
