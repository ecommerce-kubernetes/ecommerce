package com.example.order_service.api.order.saga.domain.repository;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaInstanceRepository extends JpaRepository<OrderSagaInstance, Long> {
}
