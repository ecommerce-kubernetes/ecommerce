package com.example.order_service.order.application.service.saga;

import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaService {
    private final OrderSagaInstanceRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public void createSaga(OrderSagaCommand.Create command) {
        OrderSagaInstance saga = OrderSagaInstance.create(
                command.orderNo(),
                command.step(),
                command.payload()
        );
        OrderSagaInstance saved = repository.save(saga);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(saved);
        eventPublisher.publishEvent(event);
    }
}
