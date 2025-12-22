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
        evaluateResult(result, SagaStep.PRODUCT);
    }

    public void processCouponResult(SagaProcessResult result) {
        evaluateResult(result, SagaStep.COUPON);
    }

    public void processUserResult(SagaProcessResult result) {
        evaluateResult(result, SagaStep.USER);
    }

    private void failSaga(Long sagaId, String failureReason) {
        orderSagaDomainService.fail(sagaId, failureReason);
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
    private void evaluateResult(SagaProcessResult result, SagaStep expectedStep) {
        SagaInstanceDto saga = orderSagaDomainService.getSaga(result.getSagaId());

        // 메시지 재전송과 같은 문제로 메시지가 두번 이상 발행되었을때 이미 SAGA가 진행되었으면 무시함
        if (saga.getSagaStep() != expectedStep) {
            log.warn("잘못된 sagaStep 현재 진행 상태: {}, 호출된 sagaStep: {}", saga.getSagaStep(), expectedStep);
            return;
        }

        if (saga.getSagaStatus() == SagaStatus.STARTED) { // SAGA 가 진행중인 경우
            if (result.getStatus() == SagaEventStatus.SUCCESS) {

            } else {
                // 응답받은 메시지가 실패라면 보상을 시작함
                startCompensationSequence(saga, result.getErrorCode(), result.getFailureReason());
            }
        } else if (saga.getSagaStatus() == SagaStatus.COMPENSATING) { // 보상이 진행중인 경우
            if (result.getStatus() == SagaEventStatus.SUCCESS) {

            } else {
                log.error("ERROR : 보상 실패 SagaId : {}", saga.getId());
            }
        }
    }

    private void startCompensationSequence(SagaInstanceDto saga, String errorCode, String failureReason) {
        applicationEventPublisher.publishEvent(createSagaAbortEvent(saga.getId(), saga.getOrderId(), saga.getPayload().getUserId(),
                errorCode));

        // 다음 보상 단계
        SagaStep nextStep = getNextCompensationStep(saga.getSagaStep(), saga.getPayload());

        // 다음 단계가 없다면 보상없이 바로 실패 처리
        if(nextStep == null) {
            failSaga(saga.getId(), failureReason);
            return;
        }

        // Saga 인스턴스를 보상단계로 변경
        SagaInstanceDto updateSagaInstanceDto = orderSagaDomainService.startCompensation(saga.getId(), nextStep, failureReason);
        // 단계에 맞는 Saga 보상 메시지 발행
        dispatchCompensationMessage(updateSagaInstanceDto);
    }

    private SagaStep getNextCompensationStep(SagaStep currentStep, Payload payload) {
        return switch (currentStep) {
            //보상 흐름 : 포인트복구 -> 쿠폰 복구 -> 재고 복구 순
            case USER ->
                // 쿠폰 아이디가 있다면 쿠폰 복구, 쿠폰 아이디가 없다면 상품 재고 복구
                    (payload.getCouponId() != null) ? SagaStep.COUPON : SagaStep.PRODUCT;
            case COUPON ->
                // 쿠폰 복구 이후 상품 재고 복구
                    SagaStep.PRODUCT;
            case PRODUCT ->
                // 상품 재고 복구 이후는 Saga 종료
                    null;
        };
    }

    private void dispatchCompensationMessage(SagaInstanceDto saga) {
        switch (saga.getSagaStep()) {
            // Step 이 COUPON -> 쿠폰 복구 메시지 발행
            case COUPON:
                sagaEventProducer.requestCouponCompensate(saga.getId(), saga.getOrderId(), saga.getPayload());
                break;
            // Step 이 PRODUCT -> 상품 재고 복구 메시지 발행
            case PRODUCT:
                sagaEventProducer.requestInventoryCompensate(saga.getId(), saga.getOrderId(), saga.getPayload());
                break;
        }
    }
}
