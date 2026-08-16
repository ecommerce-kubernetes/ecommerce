package com.example.order_service.saga.application.port;

import com.example.order_service.saga.domain.OrderSaga;

import java.util.Optional;

public interface OrderSagaRepository {

    OrderSaga save(OrderSaga orderSaga);

    Optional<OrderSaga> findById(Long orderSagaId);
}
