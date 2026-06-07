package com.example.order_service.order.application.service.saga;

import com.example.order_service.order.application.event.OrderSagaCompletedEvent;
import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.saga.SagaStepHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaService {
    private final OrderSagaInstanceRepository instanceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void createSaga(OrderSagaCommand.Create command) {
        OrderSagaInstance saga = OrderSagaInstance.create(
                command.orderNo(),
                command.step(),
                command.payload()
        );
        OrderSagaInstance saved = instanceRepository.save(saga);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(saved);
        eventPublisher.publishEvent(event);
    }

    public OrderSagaResult.Default getSaga(String orderNo) {
        OrderSagaInstance saga = findSagaByOrderNo(orderNo);
        return OrderSagaResult.Default.from(saga);
    }

    public void recordHistory(OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaByOrderNo(command.orderNo());
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
    }

    public void process(String orderNo, SagaStep nextStep) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.transitionTo(nextStep);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(instance);
        eventPublisher.publishEvent(event);
    }

    public void complete(String orderNo) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.complete();
        OrderSagaCompletedEvent event = OrderSagaCompletedEvent.of(instance.getOrderNo());
        eventPublisher.publishEvent(event);
    }

    public void fail(String orderNo) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.failed();
        String causeCode = instance.getCauseCode();
        OrderSagaFailedEvent event = OrderSagaFailedEvent.of(instance.getOrderNo(), causeCode);
        eventPublisher.publishEvent(event);
    }

    private OrderSagaInstance findSagaByOrderNo(String orderNo) {
        return instanceRepository.findByOrderNo(orderNo)
                .orElseThrow(IllegalArgumentException::new);
    }
}
