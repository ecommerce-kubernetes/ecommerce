package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
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
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderDto saveOrder(OrderCreationContext context){
        Order order = Order.create(context);
        Order savedOrder = orderRepository.save(order);
        return OrderDto.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderNo, Long userId) {
        Order order = getByOrderNo(orderNo);
        if (!order.isOwner(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NO_PERMISSION);
        }
        return OrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(Long userId, OrderSearchCondition condition) {
        Page<Order> orders = orderRepository.findByUserIdAndCondition(userId, condition);
        List<OrderDto> orderDtoList = orders.getContent().stream().map(OrderDto::from).toList();
        return new PageImpl<>(orderDtoList, orders.getPageable(), orders.getTotalElements());
    }

    public OrderDto preparePaymentWaiting(String orderNo){
        Order order = getByOrderNo(orderNo);
        order.preparePaymentWaiting();
        return OrderDto.from(order);
    }

    public OrderDto canceledOrder(String orderNo, OrderFailureCode code){
        Order order = getByOrderNo(orderNo);
        order.canceled(code);
        return OrderDto.from(order);
    }

    public OrderDto completePayment(PaymentCreationContext context) {
        Order order = getByOrderNo(context.getOrderNo());
        order.completePayment(context);
        orderRepository.flush();
        return OrderDto.from(order);
    }

    public OrderDto failPayment(String orderNo, OrderFailureCode orderFailureCode) {
        Order order = getByOrderNo(orderNo);
        order.paymentFailed(orderFailureCode);
        return OrderDto.from(order);
    }

    private Order getByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}
