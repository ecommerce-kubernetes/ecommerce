package com.example.order_service.api.order.saga.orchestrator;

import com.example.common.result.SagaEventStatus;
import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.facade.event.OrderEventStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaManager {

    private final OrderSagaDomainService orderSagaDomainService;
    private final SagaEventProducer sagaEventProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void startSaga(SagaStartCommand command) {
        Payload payload = Payload.from(command);
        SagaStep firstStep = SagaFlow.initialStep(payload);
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.create(command.getOrderNo(), payload, firstStep);
        dispatchProceedMessage(sagaInstanceDto);
    }

    public void processProductResult(SagaProcessResult result) {
        SagaInstanceDto saga = orderSagaDomainService.getSagaBySagaId(result.getSagaId());
        applyStepResult(saga, SagaStep.PRODUCT, result.getStatus() == SagaEventStatus.SUCCESS,
                result.getErrorCode(), result.getFailureReason());
    }

    public void processCouponResult(SagaProcessResult result) {
        SagaInstanceDto saga = orderSagaDomainService.getSagaBySagaId(result.getSagaId());
        applyStepResult(saga, SagaStep.COUPON, result.getStatus() == SagaEventStatus.SUCCESS,
                result.getErrorCode(), result.getFailureReason());
    }

    public void processUserResult(SagaProcessResult result) {
        SagaInstanceDto saga = orderSagaDomainService.getSagaBySagaId(result.getSagaId());
        applyStepResult(saga, SagaStep.USER, result.getStatus() == SagaEventStatus.SUCCESS,
                result.getErrorCode(), result.getFailureReason());
    }

    public void processPaymentResult(SagaPaymentCommand command) {
        SagaInstanceDto saga = orderSagaDomainService.getSagaByOrderNo(command.getOrderNo());
        String errorCode = command.getCode() != null ? command.getCode().name() : null;
        String failureReason = command.getFailureReason() != null ? command.getFailureReason() : null;
        applyStepResult(saga, SagaStep.PAYMENT, command.getStatus() == OrderEventStatus.SUCCESS,
                errorCode, failureReason);
    }

    public void processTimeouts() {
        //Saga 시작 시간이 5분 이전이면서 상태는 STARTED인 Saga 모두 조회
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(5);
        List<SagaInstanceDto> timeouts = orderSagaDomainService.getTimeouts(timeout);
        //조회된 SAGA 를 보상 처리함
        for (SagaInstanceDto saga : timeouts) {
            try {
                startCompensationSequence(saga, "TIMEOUT", "주문 처리 지연");
            } catch (Exception e) {
                log.error("Timeout 처리 실패 SagaId : {}", saga.getId());
            }
        }
    }

    private void applyStepResult(SagaInstanceDto saga, SagaStep expectedStep, boolean isSuccess, String errorCode, String failureReason) {

        // 메시지 재전송과 같은 문제로 메시지가 두번 이상 발행되었을때 이미 SAGA가 진행되었으면 무시함
        if (saga.getSagaStep() != expectedStep) {
            log.warn("잘못된 sagaStep 현재 진행 상태: {}, 호출된 sagaStep: {}", saga.getSagaStep(), expectedStep);
            return;
        }

        if (saga.getSagaStatus() == SagaStatus.STARTED) { // SAGA 가 진행중인 경우
            if (isSuccess) {
                proceedSequence(saga);
            } else {
                // 응답받은 메시지가 실패라면 보상을 시작함
                startCompensationSequence(saga, errorCode, failureReason);
            }
        } else if (saga.getSagaStatus() == SagaStatus.COMPENSATING) { // 보상이 진행중인 경우
            if (isSuccess) {
                // 다음 보상을 진행함
                continueCompensationSequence(saga);
            } else {
                // 보상이 실패되었으므로 에러 로그
                log.error("ERROR : 보상 실패 SagaId : {}", saga.getId());
            }
        }
    }

    private void proceedSequence(SagaInstanceDto saga) {
        SagaStep nextStep = SagaFlow.from(saga.getSagaStep()).next(saga.getPayload());
        if (nextStep == null) {
            orderSagaDomainService.finish(saga.getId());
            return;
        }

        SagaInstanceDto updateSaga = orderSagaDomainService.proceedTo(saga.getId(), nextStep);
        dispatchProceedMessage(updateSaga);
    }

    private void startCompensationSequence(SagaInstanceDto saga, String errorCode, String failureReason) {
        SagaAbortEvent abortEvent = createSagaAbortEvent(saga, errorCode);
        applicationEventPublisher.publishEvent(abortEvent);
        // 다음 보상 단계
        SagaStep nextStep = SagaFlow.from(saga.getSagaStep()).nextCompensation(saga.getPayload());
        // 다음 단계가 없다면 보상없이 바로 실패 처리
        if(nextStep == null) {
            orderSagaDomainService.fail(saga.getId(), failureReason);
            return;
        }
        // Saga 인스턴스를 보상단계로 변경
        SagaInstanceDto updateSagaInstanceDto = orderSagaDomainService.startCompensation(saga.getId(), nextStep, failureReason);
        // 단계에 맞는 Saga 보상 메시지 발행
        dispatchCompensationMessage(updateSagaInstanceDto);
    }

    private SagaAbortEvent createSagaAbortEvent(SagaInstanceDto saga, String errorCode) {
        OrderFailureCode orderFailureCode = OrderFailureCode.fromSagaErrorCode(errorCode);
        return SagaAbortEvent.of(saga.getId(), saga.getOrderNo(), saga.getPayload().getUserId(),
                orderFailureCode);
    }

    private void continueCompensationSequence(SagaInstanceDto saga) {
        // 다음 보상 단계
        SagaStep nextStep = SagaFlow.from(saga.getSagaStep()).nextCompensation(saga.getPayload());
        // 다음 단계가 없다면 보상 없이 실패 처리 진행 (이때는 실패 이유는 null)
        if (nextStep == null) {
            orderSagaDomainService.fail(saga.getId(), null);
            return;
        }
        // Saga 인스턴스 단계를 다음 보상 단계로 변경
        SagaInstanceDto updateSagaInstanceDto = orderSagaDomainService.continueCompensation(saga.getId(), nextStep);
        // 단계에 맞는 Saga 보상 메시지 발행
        dispatchCompensationMessage(updateSagaInstanceDto);
    }

    private void dispatchProceedMessage(SagaInstanceDto saga) {
        switch (saga.getSagaStep()) {
            case PRODUCT:
                sagaEventProducer.requestInventoryDeduction(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
            case COUPON:
                sagaEventProducer.requestCouponUse(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
            case USER:
                sagaEventProducer.requestUserPointUse(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
            case PAYMENT:
                SagaResourceSecuredEvent paymentWaitingEvent =
                        SagaResourceSecuredEvent.of(saga.getId(), saga.getOrderNo(), saga.getPayload().getUserId());
                applicationEventPublisher.publishEvent(paymentWaitingEvent);
        }
    }

    private void dispatchCompensationMessage(SagaInstanceDto saga) {
        switch (saga.getSagaStep()) {
            case USER:
            // Step 이 USER -> 유저 포인트 복구 메시지 발행
                sagaEventProducer.requestUserPointCompensate(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
            // Step 이 COUPON -> 쿠폰 복구 메시지 발행
            case COUPON:
                sagaEventProducer.requestCouponCompensate(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
            // Step 이 PRODUCT -> 상품 재고 복구 메시지 발행
            case PRODUCT:
                sagaEventProducer.requestInventoryCompensate(saga.getId(), saga.getOrderNo(), saga.getPayload());
                break;
        }
    }
}
