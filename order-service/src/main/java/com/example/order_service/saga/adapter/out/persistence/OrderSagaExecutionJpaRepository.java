package com.example.order_service.saga.adapter.out.persistence;

import com.example.order_service.saga.domain.ExecutionStatus;
import com.example.order_service.saga.domain.ExecutionType;
import com.example.order_service.saga.domain.OrderSagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderSagaExecutionJpaRepository extends JpaRepository<OrderSagaExecution, Long> {
    List<OrderSagaExecution> findByTypeAndStatusAndUpdatedAtBefore(ExecutionType type, ExecutionStatus status, LocalDateTime threshold);
}
