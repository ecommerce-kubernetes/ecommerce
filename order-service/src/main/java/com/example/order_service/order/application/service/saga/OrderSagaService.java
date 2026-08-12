package com.example.order_service.order.application.service.saga;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.event.OrderSagaCompletedEvent;
import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.saga.domain.tmp.OrderSagaInstance;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.saga.domain.tmp.SagaStepHistory;
import com.example.order_service.order.exception.SagaErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 SAGA 서비스
 * <p>
 * 주문 SAGA 인스턴스와 SAGA 결과 History를 저장 및 관리 후 이벤트 발행
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 08
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaService {
    private final OrderSagaInstanceRepository instanceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * SAGA 인스턴스 생성
     * <p>
     * 정방향 SagaInstance 생성 후 SagaInstance 생성 이벤트를 발행
     * </p>
     *
     * @param command SAGA 인스턴스 생성 커맨드
     */
    public void createSaga(OrderSagaCommand.Create command) {
        OrderSagaInstance saga = OrderSagaInstance.create(
                command.orderNo(),
                command.paymentId(),
                command.step(),
                command.payload()
        );
        OrderSagaInstance saved = instanceRepository.save(saga);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(saved);
        eventPublisher.publishEvent(event);
    }

    /**
     * SAGA 인스턴스 조회
     *
     * @param sagaId saga 아이디
     * @return SAGA 인스턴스 정보
     */
    public OrderSagaResult.Default getSaga(Long sagaId) {
        OrderSagaInstance instance = findSagaById(sagaId);
        return OrderSagaResult.Default.from(instance);
    }

    /**
     * 다음 SAGA 스텝 진행
     * <p>
     * SagaStepHistory를 저장하고
     * SAGA 스텝에 따라 SagaInstance 상태를 변경 후 다음 스텝 진행을 위한 이벤트 발행
     * </p>
     *
     * @param sagaId  주문 번호
     * @param nextStep 다음 단계
     * @param command  history 저장 커맨드
     */
    public void proceed(Long sagaId, SagaStep nextStep, OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaById(sagaId);
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
        instance.proceedTo(nextStep);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(instance);
        eventPublisher.publishEvent(event);
    }

    public void compensate(Long sagaId, SagaStep nextStep, OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaById(sagaId);
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
        instance.compensateTo(nextStep);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(instance);
        eventPublisher.publishEvent(event);
    }

    /**
     * Saga 완료 처리
     * <p>
     * SagaStepHistory를 저장하고 SagaInstance를 완료 처리 후 Saga 완료 이벤트 발행
     * </p>
     *
     * @param sagaId saga 아이디
     * @param command history 저장 커맨드
     */
    public void complete(Long sagaId, OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaById(sagaId);
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
        instance.complete();
        OrderSagaCompletedEvent event = OrderSagaCompletedEvent.of(instance.getOrderNo());
        eventPublisher.publishEvent(event);
    }

    /**
     * Saga 실패 처리
     * <p>
     * SagaStepHistory를 저장하고 SagaInstance를 실패 처리 후 Saga 실패 이벤트 발행
     * </p>
     *
     * @param sagaId saga 아이디
     * @param command history 저장 커맨드
     */
    public void fail(Long sagaId, OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaById(sagaId);
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
        instance.failed();
        OrderSagaFailedEvent event = OrderSagaFailedEvent.of(instance.getOrderNo(), instance.getPaymentId(), instance.getCauseCode());
        eventPublisher.publishEvent(event);
    }

    /**
     * Saga history 저장
     * @param sagaId saga 아이디
     * @param command history 저장 커맨드
     */
    public void recordHistory(Long sagaId, OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaById(sagaId);
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
    }

    private OrderSagaInstance findSagaById(Long sagaId) {
        return instanceRepository.findById(sagaId)
                .orElseThrow(() -> new BusinessException(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND));
    }

    public List<OrderSagaResult.Default> getSagasBefore(LocalDateTime threshold, int size) {
        List<SagaStatus> targetStatuses = List.of(SagaStatus.PROCESSING, SagaStatus.COMPENSATING);

        PageRequest limit = PageRequest.of(0, size);

        return instanceRepository.findTimeoutSagas(targetStatuses, threshold, limit)
                .stream()
                .map(OrderSagaResult.Default::from)
                .toList();
    }
}
