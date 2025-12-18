package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
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
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.create(command.getOrderId(), payload);
        sagaEventProducer.requestInventoryDeduction(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    public void proceedSaga(Long sagaId){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.getSaga(sagaId);
        proceed(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    //TODO 테스트 작성
    public void abortSaga(Long sagaId, String failureReason){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.abort(sagaId, failureReason);
        compensate(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    //TODO 테스트 작성
    public void compensateSaga(Long sagaId) {
    }

    private void compensate(Long sagaId, SagaStep sagaStep, Payload payload) {
        switch (sagaStep) {
            case USER:
                compensateCoupon();
                break;
            case COUPON:
                compensateInventory();
                break;
            case PRODUCT:
                failSaga();
                break;
        }
    }

    private void compensateCoupon(){
    }

    private void compensateInventory() {
    }

    private void failSaga() {
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
                completeSaga(sagaId);
                break;
        }
    }

    private void executeCoupon(Long sagaId, Payload payload) {
        if (payload.getCouponId() == null) {
            // CouponId 가 null 이므로 다음(포인트 차감) 단계로 건너뛰기
            proceed(sagaId, SagaStep.COUPON, payload);
            return;
        }
        SagaInstanceDto instance = orderSagaDomainService.nextStepToCoupon(sagaId);
        sagaEventProducer.requestCouponUse(instance.getId(), instance.getOrderId(), instance.getPayload());
    }

    private void executeUsePoint(Long sagaId, Payload payload) {
        if (payload.getUseToPoint() == null || payload.getUseToPoint() == 0) {
            // 사용 포인트가 없으므로 Saga 완료 단계로 건너뛰기
            proceed(sagaId, SagaStep.USER, payload);
            return;
        }
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.nextStepToUser(sagaId);
        sagaEventProducer.requestUserPointUse(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    private void completeSaga(Long sagaId) {
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.finish(sagaId);
        SagaCompletedEvent completedEvent = SagaCompletedEvent
                .of(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload().getUserId());
        applicationEventPublisher.publishEvent(completedEvent);
    }
}
