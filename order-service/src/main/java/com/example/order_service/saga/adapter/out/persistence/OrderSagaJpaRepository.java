package com.example.order_service.saga.adapter.out.persistence;

import com.example.order_service.saga.domain.OrderSaga;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaJpaRepository extends JpaRepository<OrderSaga, Long> {
}
