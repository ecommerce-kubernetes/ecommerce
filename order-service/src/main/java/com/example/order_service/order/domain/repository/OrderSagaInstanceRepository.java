package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.saga.OrderSagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaInstanceRepository extends JpaRepository<OrderSagaInstance, Long> {
}
