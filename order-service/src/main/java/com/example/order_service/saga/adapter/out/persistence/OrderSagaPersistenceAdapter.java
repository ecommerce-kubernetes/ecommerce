package com.example.order_service.saga.adapter.out.persistence;

import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.OrderSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderSagaPersistenceAdapter implements OrderSagaRepository {

    private final OrderSagaJpaRepository jpaRepository;

    @Override
    public OrderSaga save(OrderSaga orderSaga) {
        return jpaRepository.save(orderSaga);
    }

    @Override
    public Optional<OrderSaga> findById(Long orderSagaId) {
        return jpaRepository.findById(orderSagaId);
    }
}
