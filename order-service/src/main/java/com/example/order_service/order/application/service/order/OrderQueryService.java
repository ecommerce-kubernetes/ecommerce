package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.repository.OrderSearchRepository;
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

    private final OrderSearchRepository orderSearchRepository;

    public OrderResult getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByOrderIdAndOrdererId(orderId, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.from(order);
    }

    public Page<OrderSummaryResult> getOrders(Long userId, OrderSearchCommand command) {
        return null;
    }

    public OrderResultDeprecated.Detail getOrder(String orderNo) {
        return null;
    }

    public List<OrderResultDeprecated.Summary> getPendingOrdersBefore(LocalDateTime threshold, int size) {
        List<Order> orders = orderSearchRepository.findOrdersBefore(threshold, size);
        return orders.stream().map(OrderResultDeprecated.Summary::from).toList();
    }
}
