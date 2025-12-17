package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaManager {

    private final OrderSagaDomainService orderSagaDomainService;
    private final SagaEventProducer sagaEventProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void startSaga(SagaStartCommand command) {
        Payload payload = Payload.from(command);
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.saveOrderSagaInstance(command.getOrderId(), payload);
        sagaEventProducer.requestInventoryDeduction(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    public void proceedSaga(Long sagaId){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.getOrderSagaInstance(sagaId);
        proceed(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    private void proceed(Long sagaId, SagaStep sagaStep, Payload payload) {
        switch (sagaStep) {
            case PRODUCT:
                executeCoupon(sagaId, payload);
                break;
            case COUPON:
                executeUsePoint(sagaId, payload);
                break;
            case USER:
                executeCompleteSaga(sagaId);
                break;
        }
    }

    private void executeCoupon(Long sagaId, Payload payload) {
        if (payload.getCouponId() == null) {
            // CouponId 가 null 이므로 다음(포인트 차감) 단계로 건너뛰기
            proceed(sagaId, SagaStep.COUPON, payload);
            return;
        }
        SagaInstanceDto instance = orderSagaDomainService.updateToCouponSagaInstance(sagaId);
        sagaEventProducer.requestCouponUse(instance.getId(), instance.getOrderId(), instance.getPayload());
    }

    private void executeUsePoint(Long sagaId, Payload payload) {
        if (payload.getUseToPoint() == null || payload.getUseToPoint() == 0) {
            // 사용 포인트가 없으므로 Saga 완료 단계로 건너뛰기
            proceed(sagaId, SagaStep.USER, payload);
            return;
        }
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.updateToPointSagaInstance(sagaId);
        sagaEventProducer.requestUserPointUse(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    private void executeCompleteSaga(Long sagaId) {
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.updateToCompleteSagaInstance(sagaId);
        SagaCompletedEvent completedEvent = SagaCompletedEvent
                .of(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload().getUserId());
        applicationEventPublisher.publishEvent(completedEvent);
    }

    public void abortSaga(Long sagaId, String failureReason){

    }
}
