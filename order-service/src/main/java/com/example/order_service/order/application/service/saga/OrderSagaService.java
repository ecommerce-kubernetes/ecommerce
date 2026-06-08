package com.example.order_service.order.application.service.saga;

import com.example.order_service.order.application.event.OrderSagaCompletedEvent;
import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.saga.SagaStepHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param orderNo 주문 번호
     * @return SAGA 인스턴스 정보
     */
    public OrderSagaResult.Default getSaga(String orderNo) {
        OrderSagaInstance saga = findSagaByOrderNo(orderNo);
        return OrderSagaResult.Default.from(saga);
    }

    /**
     * SAGA History 저장
     * <p>
     * 해당 SAGA의 RecordHistory를 저장
     * </p>
     *
     * @param command History 저장 커맨드
     */
    public void recordHistory(OrderSagaCommand.RecordHistory command) {
        OrderSagaInstance instance = findSagaByOrderNo(command.orderNo());
        SagaStepHistory history = SagaStepHistory.from(command.step(), command.status(), command.code());
        instance.addHistory(history);
    }

    /**
     * 다음 SAGA 스텝 진행
     * <p>
     * SAGA 스텝에 따라 SagaInstance 상태를 변경 후 다음 스텝 진행을 위한 이벤트 발행
     * </p>
     *
     * @param orderNo  주문 번호
     * @param nextStep 다음 단계
     */
    public void process(String orderNo, SagaStep nextStep) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.transitionTo(nextStep);
        OrderSagaProcessEvent event = OrderSagaProcessEvent.from(instance);
        eventPublisher.publishEvent(event);
    }

    /**
     * Saga 완료 처리
     * <p>
     * SagaInstance를 완료 처리하고 Saga 완료 이벤트 발행
     * </p>
     *
     * @param orderNo 주문 번호
     */
    public void complete(String orderNo) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.complete();
        OrderSagaCompletedEvent event = OrderSagaCompletedEvent.of(instance.getOrderNo());
        eventPublisher.publishEvent(event);
    }

    /**
     * Saga 실패 처리
     * <p>
     * SagaInstance를 실패 처리하고 Saga 실패 이벤트 발행
     * </p>
     *
     * @param orderNo 주문 번호
     */
    public void fail(String orderNo) {
        OrderSagaInstance instance = findSagaByOrderNo(orderNo);
        instance.failed();
        String causeCode = instance.getCauseCode();
        OrderSagaFailedEvent event = OrderSagaFailedEvent.of(instance.getOrderNo(), causeCode);
        eventPublisher.publishEvent(event);
    }

    private OrderSagaInstance findSagaByOrderNo(String orderNo) {
        return instanceRepository.findByOrderNo(orderNo)
                .orElseThrow(IllegalArgumentException::new);
    }
}
