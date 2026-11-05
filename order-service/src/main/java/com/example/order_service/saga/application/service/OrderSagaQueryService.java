package com.example.order_service.saga.application.service;

import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.application.service.dto.result.OrderSagaExecutionResult;
import com.example.order_service.saga.domain.ExecutionStatus;
import com.example.order_service.saga.domain.ExecutionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderSagaQueryService {

    private final OrderSagaRepository orderSagaRepository;

    public List<OrderSagaExecutionResult> getForwardPendingExecutionsBefore(LocalDateTime threshold) {
        return orderSagaRepository.findExecutionsByTypeAndStatusAndUpdatedAtBefore(ExecutionType.FORWARD, ExecutionStatus.PENDING, threshold)
                .stream()
                .map(OrderSagaExecutionResult::from)
                .toList();
    }

    public List<OrderSagaExecutionResult> getCompensatePendingExecutionsBefore(LocalDateTime threshold) {
        return orderSagaRepository.findExecutionsByTypeAndStatusAndUpdatedAtBefore(ExecutionType.COMPENSATE, ExecutionStatus.PENDING, threshold)
                .stream()
                .map(OrderSagaExecutionResult::from)
                .toList();
    }
}
