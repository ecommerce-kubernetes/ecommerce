package com.example.order_service.api.order.saga.orchestrator;

import com.example.common.result.SagaEventStatus;
import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
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

    public void processProductResult(SagaProcessResult result) {
        SagaInstanceDto sagaInstance = orderSagaDomainService.getSaga(result.getSagaId());
        if (sagaInstance.getSagaStatus() == SagaStatus.STARTED) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                proceed(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                abortSaga(sagaInstance.getId(), result.getErrorCode(), result.getFailureReason());
            }
        } else if (sagaInstance.getSagaStatus() == SagaStatus.COMPENSATING) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                compensate(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                log.error("상품 보상 실패");
            }
        }
    }

    public void processCouponResult(SagaProcessResult result) {
        SagaInstanceDto sagaInstance = orderSagaDomainService.getSaga(result.getSagaId());
        if (sagaInstance.getSagaStatus() == SagaStatus.STARTED) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                proceed(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                abortSaga(sagaInstance.getId(), result.getErrorCode(), result.getFailureReason());
            }
        } else if (sagaInstance.getSagaStatus() == SagaStatus.COMPENSATING) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                compensate(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                log.error("쿠폰 보상 실패");
            }
        }
    }

    public void processUserResult(SagaProcessResult result) {
        SagaInstanceDto sagaInstance = orderSagaDomainService.getSaga(result.getSagaId());
        if (sagaInstance.getSagaStatus() == SagaStatus.STARTED) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                proceed(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                abortSaga(sagaInstance.getId(), result.getErrorCode(), result.getFailureReason());
            }
        } else if (sagaInstance.getSagaStatus() == SagaStatus.COMPENSATING) {
            if (result.getStatus() == SagaEventStatus.SUCCESS) {
                compensate(sagaInstance.getId(), sagaInstance.getSagaStep(), sagaInstance.getPayload());
            } else {
                log.error("포인트 보상 실패");
            }
        }
    }

    public void proceedSaga(Long sagaId){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.getSaga(sagaId);
        proceed(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    public void abortSaga(Long sagaId, String errorCode, String failureReason){
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.abort(sagaId, failureReason);
        SagaAbortEvent sagaAbortEvent = createSagaAbortEvent(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload().getUserId(),
                errorCode);
        applicationEventPublisher.publishEvent(sagaAbortEvent);
        compensate(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    public void compensateSaga(Long sagaId) {
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.getSaga(sagaId);
        compensate(sagaInstanceDto.getId(), sagaInstanceDto.getSagaStep(), sagaInstanceDto.getPayload());
    }

    private void compensate(Long sagaId, SagaStep sagaStep, Payload payload) {
        switch (sagaStep) {
            case USER:
                compensateCoupon(sagaId, payload);
                break;
            case COUPON:
                compensateInventory(sagaId, payload);
                break;
            case PRODUCT:
                failSaga(sagaId);
                break;
        }
    }

    private void compensateCoupon(Long sagaId, Payload payload){
        if (payload.getCouponId() == null) {
            compensate(sagaId, SagaStep.COUPON, payload);
            return;
        }
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.compensateTo(sagaId, SagaStep.COUPON);
        sagaEventProducer.requestCouponCompensate(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    private void compensateInventory(Long sagaId, Payload payload) {
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.compensateTo(sagaId, SagaStep.PRODUCT);
        sagaEventProducer.requestInventoryCompensate(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    private void failSaga(Long sagaId) {
        orderSagaDomainService.fail(sagaId);
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
        SagaInstanceDto instance = orderSagaDomainService.proceedTo(sagaId, SagaStep.COUPON);
        sagaEventProducer.requestCouponUse(instance.getId(), instance.getOrderId(), instance.getPayload());
    }

    private void executeUsePoint(Long sagaId, Payload payload) {
        if (payload.getUseToPoint() == null || payload.getUseToPoint() == 0) {
            // 사용 포인트가 없으므로 Saga 완료 단계로 건너뛰기
            proceed(sagaId, SagaStep.USER, payload);
            return;
        }
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.proceedTo(sagaId, SagaStep.USER);
        sagaEventProducer.requestUserPointUse(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload());
    }

    private void completeSaga(Long sagaId) {
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.finish(sagaId);
        SagaCompletedEvent completedEvent = SagaCompletedEvent
                .of(sagaInstanceDto.getId(), sagaInstanceDto.getOrderId(), sagaInstanceDto.getPayload().getUserId());
        applicationEventPublisher.publishEvent(completedEvent);
    }

    private SagaAbortEvent createSagaAbortEvent(Long sagaId, Long orderId, Long userId, String errorCode) {

        OrderFailureCode failureCode = OrderFailureCode.UNKNOWN;
        if(errorCode.equals("OUT_OF_STOCK")) {
            failureCode = OrderFailureCode.OUT_OF_STOCK;
        } else if (errorCode.equals("INVALID_COUPON")) {
            failureCode = OrderFailureCode.INVALID_COUPON;
        } else if (errorCode.equals("INSUFFICIENT_POINT")){
            failureCode = OrderFailureCode.POINT_SHORTAGE;
        }

        return SagaAbortEvent.of(sagaId, orderId, userId, failureCode);
    }
}
