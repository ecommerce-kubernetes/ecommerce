package com.example.order_service.order.application.saga.listener;

import com.example.order_service.order.application.saga.dto.SagaCommand;
import com.example.order_service.order.application.service.order.OrderAppService;
import com.example.order_service.order.application.event.OrderCreatedEvent;
import com.example.order_service.order.application.event.PaymentCompletedEvent;
import com.example.order_service.order.application.event.PaymentFailedEvent;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.application.saga.domain.model.SagaStep;
import com.example.order_service.order.application.saga.orchestrator.SagaManager;
import com.example.order_service.order.application.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.order.application.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.order.application.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.order.application.saga.orchestrator.event.SagaResourceSecuredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderAppService orderAppService;
    private final SagaManager sagaManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        SagaCommand.StartSaga command = mappingStartSagaCommand(event);
        sagaManager.startSaga(command);
    }

    private SagaCommand.StartSaga mappingStartSagaCommand(OrderCreatedEvent event) {
        SagaCommand.PointDeduction pointDeduction = SagaCommand.PointDeduction.of(event.getUserId(), event.getUsedPoint());
        SagaCommand.CouponUsage couponUsage = SagaCommand.CouponUsage.of(event.getUserId(), event.getCartCouponId(), event.getItemCouponIds());
        List<SagaCommand.StockDeduction> stockDeductions = event.getOrderedItems().stream()
                .map(item -> SagaCommand.StockDeduction.of(item.getProductVariantId(), item.getQuantity())).toList();
        return SagaCommand.StartSaga.builder()
                .orderNo(event.getOrderNo())
                .pointDeduction(pointDeduction)
                .couponUsage(couponUsage)
                .stockDeductions(stockDeductions)
                .build();
    }

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, event.getOrderNo(), true, null, null);
        sagaManager.handleStepResult(command);
    }

    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, event.getOrderNo(), false, event.getCode(),
                event.getFailureReason());
        sagaManager.handleStepResult(command);
    }

    @EventListener
    public void handleSagaCompleted(SagaResourceSecuredEvent event){
        orderAppService.preparePayment(event.getOrderNo());
    }

    @EventListener
    public void handleSagaAborted(SagaAbortEvent event) {
        OrderFailureCode orderFailureCode = mapToOrderFailureCode(event.getFailureCode());
        orderAppService.processOrderFailure(event.getOrderNo(), orderFailureCode);
    }

    private OrderFailureCode mapToOrderFailureCode(String errorCode) {
        if (errorCode == null) return OrderFailureCode.UNKNOWN;

        return switch (errorCode) {
            case "INSUFFICIENT_POINT" -> OrderFailureCode.INSUFFICIENT_POINT;
            case "INVALID_COUPON" -> OrderFailureCode.INVALID_COUPON;
            case "COUPON_EXPIRED" -> OrderFailureCode.COUPON_EXPIRED;
            case "INSUFFICIENT_STOCK" -> OrderFailureCode.INSUFFICIENT_STOCK;
            case "PAYMENT_INSUFFICIENT_BALANCE" -> OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE;
            case "PAYMENT_ALREADY_PROCEED_PAYMENT" -> OrderFailureCode.ALREADY_PROCEED_PAYMENT;
            case "SAGA_TIMEOUT" -> OrderFailureCode.SAGA_TIMEOUT;
            default -> OrderFailureCode.UNKNOWN;
        };
    }
}
