package com.example.order_service.saga.application.service;

import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaCommandService {

    private final IdGenerator idGenerator;
    private final OrderSagaRepository orderSagaRepository;

    public Long createOrderSaga(CreateOrderSagaContext context){
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);
        OrderSaga savedSaga = orderSagaRepository.save(orderSaga);
        return savedSaga.getId();
    }
}
