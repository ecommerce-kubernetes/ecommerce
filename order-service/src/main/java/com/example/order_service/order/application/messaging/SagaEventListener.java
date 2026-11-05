package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.payment.application.service.PaymentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Saga 관련 이벤트 수신 리스너
 *
 * @author 최민식
 * @since 2026. 06. 08
 */
@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final PaymentFacade paymentFacade;
    private final SagaMessageDispatcher dispatcher;

    /**
     * 주문 SAGA 진행
     * <p>
     * SAGA 진행 이벤트 수신 후 Saga 메시지 디스패처 호출
     * </p>
     *
     * @param event SAGA 진행 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSagaProcess(OrderSagaProcessEvent event) {
        SagaMessage message = SagaMessage.from(event);
        dispatcher.dispatch(message);
    }

    /**
     * 주문 SAGA 실패
     * <p>
     * SAGA 실패 이벤트 수신후 결제 보상 호출
     * </p>
     *
     * @param event SAGA 실패 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRefound(OrderSagaFailedEvent event) {
        paymentFacade.revert(event.getPaymentId(), event.getCode());
    }
}
