package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.SagaService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.handler.SagaStepHandler;
import com.example.order_service.api.order.saga.orchestrator.handler.SagaStepHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaManager {

    private final SagaService sagaService;
    private final SagaStepHandlerFactory handlerFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void startSaga(SagaStartCommand command) {
        // payload 생성
        Payload payload = Payload.from(command);
        // 첫번째 단계 생성 [상품 재고 감소]
        SagaStep firstStep = SagaFlow.initialStep(payload);
        // saga 인스턴스 저장
        SagaInstanceDto sagaInstanceDto = sagaService.initialize(command.getOrderNo(), payload, firstStep);
        // sagaHandler를 찾아 saga를 진행
        processStep(sagaInstanceDto);
    }

    public void handleStepResult(SagaStepResultCommand command) {
        SagaInstanceDto saga = sagaService.getSagaByOrderNo(command.getOrderNo());
        if (saga.getSagaStep() != command.getStep()) {
            log.warn("이미 처리되었거나 잘못된 단계의 응답입니다. current={}, result={}",
                    saga.getSagaStep(), saga);
            return;
        }

        if (saga.getSagaStatus() == SagaStatus.STARTED) {
            // 현재 saga가 진행중인 경우
            // 다음 saga 진행
            handleProceedFlow(saga, command);
        } else {
            // 현재 보상이 진행중인 경우
            // 다음 보상 진행
            handleCompensateFlow(saga, command);
        }
    }

    private void handleProceedFlow(SagaInstanceDto saga, SagaStepResultCommand command) {
        if (command.isSuccess()) {
            // saga 결과가 성공인 경우 다음 스텝을 진행
            advanceSequence(saga);
        } else {
            // saga 결과가 실패인 경우 보상 시작 로직을 진행
            startCompensationSequence(saga, command.getErrorCode(), command.getFailureReason());
        }
    }

    private void handleCompensateFlow(SagaInstanceDto saga, SagaStepResultCommand command) {
        if (command.isSuccess()) {
            continueCompensationSequence(saga);
        } else {
            log.error("보상 실패 [위험]");
        }
    }

    private void advanceSequence(SagaInstanceDto saga) {
        // 다음 saga 스텝 조회
        SagaStep nextStep = SagaFlow.from(saga.getSagaStep()).next(saga.getPayload());
        if (nextStep == null) {
            // 다음 saga 스텝이 없으면 saga 종료
            sagaService.finish(saga.getId());
            return;
        }
        // saga 인스턴스 상태 업데이트
        SagaInstanceDto updateSaga = sagaService.proceedTo(saga.getId(), nextStep);
        // 핸들러 찾아 처리 진행
        processStep(updateSaga);
    }

    private void startCompensationSequence(SagaInstanceDto saga, String errorCode, String failureReason) {
        SagaAbortEvent abortEvent = SagaAbortEvent.of(saga.getId(), saga.getOrderNo(), saga.getPayload().getUserId(), errorCode);
        applicationEventPublisher.publishEvent(abortEvent);
        SagaStep compensationStep = SagaFlow.from(saga.getSagaStep()).nextCompensation(saga.getPayload());
        if (compensationStep == null) {
            sagaService.fail(saga.getId(), failureReason);
        } else {
            SagaInstanceDto updateSaga = sagaService.startCompensation(saga.getId(), compensationStep, failureReason);
            compensateStep(updateSaga);
        }
    }

    private void continueCompensationSequence(SagaInstanceDto saga) {
        // 다음 보상 단계
        SagaStep nextStep = SagaFlow.from(saga.getSagaStep()).nextCompensation(saga.getPayload());
        // 다음 단계가 없다면 보상 없이 실패 처리 진행 (이때는 실패 이유는 null)
        if (nextStep == null) {
            sagaService.fail(saga.getId(), null);
            return;
        }
        // Saga 인스턴스 단계를 다음 보상 단계로 변경
        SagaInstanceDto updateSagaInstanceDto = sagaService.continueCompensation(saga.getId(), nextStep);
        // 단계에 맞는 Saga 보상 메시지 발행
        compensateStep(updateSagaInstanceDto);
    }

    public void processTimeouts() {
        //Saga 시작 시간이 5분 이전이면서 상태는 STARTED인 Saga 모두 조회
//        LocalDateTime timeout = LocalDateTime.now().minusMinutes(5);
//        List<SagaInstanceDto> timeouts = sagaService.getTimeouts(timeout);
//        //조회된 SAGA 를 보상 처리함
//        for (SagaInstanceDto saga : timeouts) {
//            try {
//                oldstartCompensationSequence(saga, "TIMEOUT", "주문 처리 지연");
//            } catch (Exception e) {
//                log.error("Timeout 처리 실패 SagaId : {}", saga.getId());
//            }
//        }
    }

    private void processStep(SagaInstanceDto saga) {
        SagaStepHandler handler = handlerFactory.getHandler(saga.getSagaStep());
        handler.process(saga.getId(), saga.getOrderNo(), saga.getPayload());
    }

    private void compensateStep(SagaInstanceDto saga) {
        SagaStepHandler handler = handlerFactory.getHandler(saga.getSagaStep());
        handler.compensate(saga.getId(), saga.getOrderNo(), saga.getPayload());
    }
}
