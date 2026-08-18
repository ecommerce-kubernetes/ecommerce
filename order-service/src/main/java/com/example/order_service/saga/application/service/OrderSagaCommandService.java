package com.example.order_service.saga.application.service;

import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.exception.SagaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaCommandService {

    private final IdGenerator idGenerator;
    private final OrderSagaRepository orderSagaRepository;

    public Long createOrderSaga(CreateOrderSagaContext context) {
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);
        OrderSaga savedSaga = orderSagaRepository.save(orderSaga);
        return savedSaga.getId();
    }

    public void completeForward(Long sagaId, Long executionId) {
        OrderSaga orderSaga = getOrderSaga(sagaId);
        orderSaga.completeForward(executionId, idGenerator);
        orderSagaRepository.save(orderSaga);
    }

    public void failForward(Long sagaId, Long executionId, String failureReason) {
        OrderSaga orderSaga = getOrderSaga(sagaId);
        orderSaga.failForward(executionId, failureReason, idGenerator);
        orderSagaRepository.save(orderSaga);
    }

    public void completeCompensate(Long sagaId, Long executionId) {
        OrderSaga orderSaga = getOrderSaga(sagaId);
        orderSaga.completeCompensate(executionId, idGenerator);
        orderSagaRepository.save(orderSaga);
    }

    public void failCompensate(Long sagaId, Long executionId) {
        OrderSaga orderSaga = getOrderSaga(sagaId);
        orderSaga.failCompensate(executionId);
        orderSagaRepository.save(orderSaga);
    }

    private OrderSaga getOrderSaga(Long sagaId) {
        return orderSagaRepository.findById(sagaId)
                .orElseThrow(() -> new SagaNotFoundException("사가를 찾을 수 없습니다"));
    }
}
