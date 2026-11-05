package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public OrderResult getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByOrderIdAndOrdererId(orderId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.from(order);
    }

    public Page<OrderSummaryResult> getOrders(Long userId, OrderSearchCommand command) {
        Page<Order> orders = orderRepository.searchOrders(userId, command);
        return orders.map(OrderSummaryResult::from);
    }

    public List<OrderSummaryResult> getOrdersByPendingAndCreatedAtBefore(LocalDateTime threshold) {
        List<Order> orders = orderRepository.findOrdersByStatusAndCreatedAtBefore(OrderStatus.PENDING, threshold);
        return orders.stream().map(OrderSummaryResult::from).toList();
    }
}
