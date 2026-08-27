package com.example.order_service.saga.application.port;

import com.example.order_service.saga.domain.ExecutionStatus;
import com.example.order_service.saga.domain.ExecutionType;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.OrderSagaExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderSagaRepository {

    OrderSaga save(OrderSaga orderSaga);

    Optional<OrderSaga> findById(Long orderSagaId);

    List<OrderSagaExecution> findExecutionsByTypeAndStatusAndUpdatedAtBefore(ExecutionType type, ExecutionStatus status, LocalDateTime threshold);
}
