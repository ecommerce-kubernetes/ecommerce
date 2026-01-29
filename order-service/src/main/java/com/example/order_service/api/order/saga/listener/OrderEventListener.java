package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.facade.OrderFacade;
import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import com.example.order_service.api.order.facade.event.PaymentCompletedEvent;
import com.example.order_service.api.order.facade.event.PaymentFailedEvent;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderFacade orderFacade;
    private final SagaManager sagaManager;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        SagaStartCommand command = SagaStartCommand.from(event);
        sagaManager.startSaga(command);
    }

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, event.getOrderNo(), true, null, null);
        sagaManager.handleStepResult(command);
    }

    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, event.getOrderNo(), false, event.getCode().name(),
                event.getFailureReason());
        sagaManager.handleStepResult(command);
    }

    @EventListener
    public void handleSagaCompleted(SagaResourceSecuredEvent event){
        orderFacade.preparePayment(event.getOrderNo());
    }

    @EventListener
    public void handleSagaAborted(SagaAbortEvent event) {
        OrderFailureCode orderFailureCode = mapToOrderFailureCode(event.getFailureCode());
        orderFacade.processOrderFailure(event.getOrderNo(), orderFailureCode);
    }

    private OrderFailureCode mapToOrderFailureCode(String errorCode) {
        if (errorCode == null) return OrderFailureCode.UNKNOWN;

        return switch (errorCode) {
            case "INSUFFICIENT_POINT" -> OrderFailureCode.INSUFFICIENT_POINT;
            case "INVALID_COUPON" -> OrderFailureCode.INVALID_COUPON;
            case "COUPON_EXPIRED" -> OrderFailureCode.COUPON_EXPIRED;
            case "INSUFFICIENT_STOCK" -> OrderFailureCode.INSUFFICIENT_STOCK;
            default -> OrderFailureCode.SYSTEM_ERROR;
        };
    }
}
