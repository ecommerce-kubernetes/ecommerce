package com.example.order_service.order.infrastructure.adaptor.persistence;

import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderPersistenceAdaptor implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findByOrderIdAndOrdererId(Long orderId, Long ordererId) {
        return orderJpaRepository.findByIdAndOrderer_UserId(orderId, ordererId);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
}
