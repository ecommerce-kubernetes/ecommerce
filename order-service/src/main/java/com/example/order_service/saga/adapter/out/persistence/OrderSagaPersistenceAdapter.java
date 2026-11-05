package com.example.order_service.saga.adapter.out.persistence;

import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.ExecutionStatus;
import com.example.order_service.saga.domain.ExecutionType;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.OrderSagaExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderSagaPersistenceAdapter implements OrderSagaRepository {

    private final OrderSagaJpaRepository jpaRepository;
    private final OrderSagaExecutionJpaRepository executionJpaRepository;

    @Override
    public OrderSaga save(OrderSaga orderSaga) {
        return jpaRepository.save(orderSaga);
    }

    @Override
    public Optional<OrderSaga> findById(Long orderSagaId) {
        return jpaRepository.findById(orderSagaId);
    }

    @Override
    public List<OrderSagaExecution> findExecutionsByTypeAndStatusAndUpdatedAtBefore(ExecutionType type, ExecutionStatus status, LocalDateTime threshold) {
        return executionJpaRepository.findByTypeAndStatusAndUpdatedAtBefore(type, status, threshold);
    }
}
