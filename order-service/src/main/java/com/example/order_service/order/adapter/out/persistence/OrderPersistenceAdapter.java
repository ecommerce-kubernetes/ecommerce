package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderQueryDslRepository orderQueryDslRepository;

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderIdAndOrdererId(Long orderId, Long ordererId) {
        return orderJpaRepository.findByIdAndOrderer_UserId(orderId, ordererId);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> searchOrders(Long ordererId, OrderSearchCommand command) {
        return orderQueryDslRepository.searchOrders(ordererId, command);
    }
}
