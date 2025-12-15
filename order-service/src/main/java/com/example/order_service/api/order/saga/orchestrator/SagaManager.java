package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaManager {

    private final OrderSagaDomainService orderSagaDomainService;
    private final SagaEventProducer sagaEventProducer;
    public void startSaga(SagaStartCommand command) {
        Payload payload = Payload.from(command);

        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.saveOrderSagaInstance(command.getOrderId(), payload);
    }

}
