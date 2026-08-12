package com.example.order_service.order.application.orchestrator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
import com.example.order_service.order.application.service.saga.OrderSagaService;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.saga.domain.tmp.SagaStatus;
import com.example.order_service.saga.domain.tmp.SagaStep;
import com.example.order_service.saga.domain.tmp.StepResult;
import com.example.order_service.order.domain.vo.SagaPayload;
import com.example.order_service.order.infrastructure.messaging.dto.SagaReplyMessage;
import com.example.order_service.order.infrastructure.messaging.dto.SagaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaManager {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final OrderSagaService orderSagaService;

    /**
     * 주문 SAGA 시작
     * <p>
     * 주문의 상태를 결제 상태로 변경하고 SAGA를 진행한다
     * </p>
     *
     * @param orderNo 주문 번호
     */
    @Transactional
    public void startSaga(String orderNo, Long paymentId) {
        orderCommandService.changePaid(orderNo);
        OrderResultDeprecated.Detail order = orderQueryService.getOrder(orderNo);
        SagaPayload payload = createPayload(order);
        OrderSagaCommand.Create command = OrderSagaCommand.Create.of(order.orderNo(), paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        orderSagaService.createSaga(command);
    }

    private SagaPayload createPayload(OrderResultDeprecated.Detail order) {
        List<Long> itemCouponIds = order.items().stream()
                .map(item -> item.itemCoupon().getItemCouponId())
                .filter(java.util.Objects::nonNull)
                .toList();
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(order.usedPoints());
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(order.cartCoupon().getCartCouponId(), itemCouponIds);
        List<SagaPayload.ItemPayload> itemPayloads = order.items().stream().map(item -> SagaPayload.ItemPayload
                .of(item.product().getProductVariantId(), item.quantity())).toList();
        return SagaPayload.of(order.orderer().getUserId(), itemPayloads, couponPayload, pointPayload);
    }

    /**
     * 다음 SAGA 진행
     *
     * @param message 수신 메시지
     */
    @Transactional
    public void handleReply(SagaReplyMessage message) {
        OrderSagaResult.Default saga = orderSagaService.getSaga(message.getSagaId());
        if (saga.currentStep() != message.getStep()) {
            log.warn("Saga 지연/중복 응답 주문:{}, 스텝:{}", message.getOrderNo(), message.getStep());
            return;
        }
        /*
        [NOTE] Saga 흐름
        SAGA 흐름은 재고 차감, 쿠폰 무효화, 포인트 차감 순으로 진행되고
        보상 처리는 정방향의 역순으로 진행된다
         */
        if (message.getResult() == SagaResult.SUCCESS) {
            processSuccess(saga, message);
        } else {
            processFailure(saga, message);
        }
    }

    private void processSuccess(OrderSagaResult.Default saga, SagaReplyMessage message) {
        OrderSagaCommand.RecordHistory historyCommand = OrderSagaCommand.RecordHistory.of(
                saga.orderNo(), StepResult.COMPLETED, saga.currentStep(), message.getCode()
        );
        SagaPayload payload = saga.payload();
        switch (saga.currentStep()) {
            case INVENTORY_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.proceed(saga.sagaId(), SagaStep.COUPON_USE_PENDING, historyCommand);
                } else if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.proceed(saga.sagaId(), SagaStep.POINTS_DEDUCT_PENDING, historyCommand);
                } else {
                    orderCommandService.changeCompleted(saga.orderNo());
                    orderSagaService.complete(saga.sagaId(), historyCommand);
                }
            }

            case INVENTORY_RESTORE_PENDING -> {
                orderCommandService.changeFailed(saga.orderNo(), saga.causeCode());
                orderSagaService.fail(saga.sagaId(), historyCommand);
            }

            case COUPON_USE_PENDING -> {
                if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.proceed(saga.sagaId(), SagaStep.POINTS_DEDUCT_PENDING, historyCommand);
                } else {
                    orderCommandService.changeCompleted(saga.orderNo());
                    orderSagaService.complete(saga.sagaId(), historyCommand);
                }
            }

            case COUPON_RESTORE_PENDING -> orderSagaService.compensate(saga.sagaId(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);

            case POINTS_DEDUCT_PENDING -> {
                orderCommandService.changeCompleted(saga.orderNo());
                orderSagaService.complete(saga.sagaId(), historyCommand);
            }
        }
    }

    private void processFailure(OrderSagaResult.Default saga, SagaReplyMessage message) {
        OrderSagaCommand.RecordHistory historyCommand = OrderSagaCommand.RecordHistory.of(
                saga.orderNo(), StepResult.FAILED, saga.currentStep(), message.getCode()
        );
        SagaPayload payload = saga.payload();
        switch (saga.currentStep()) {
            case INVENTORY_DEDUCT_PENDING -> {
                orderCommandService.changeFailed(saga.orderNo(), saga.causeCode());
                orderSagaService.fail(saga.sagaId(), historyCommand);
            }
            case COUPON_USE_PENDING -> orderSagaService.compensate(saga.sagaId(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);
            case POINTS_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.compensate(saga.sagaId(), SagaStep.COUPON_RESTORE_PENDING, historyCommand);
                } else {
                    orderSagaService.compensate(saga.sagaId(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);
                }
            }
            case COUPON_RESTORE_PENDING, INVENTORY_RESTORE_PENDING -> {
                orderSagaService.recordHistory(saga.sagaId(), historyCommand);
                log.error("[FATAL ERROR] 보상 트랜잭션 실패로 SAGA 중단 orderNo:{}, step:{}, cause:{}",
                        saga.orderNo(), saga.currentStep(), message.getCode());
            }
        }
    }

    @Transactional
    public void timeoutSaga(Long sagaId) {
        OrderSagaResult.Default saga = orderSagaService.getSaga(sagaId);
        if (saga.status() != SagaStatus.STARTED && saga.status() != SagaStatus.COMPENSATING) {
            log.info("SAGA 타임아웃 처리 중단: 이미 완료되거나 종료된 인스턴스 sagaId={}", sagaId);
            return;
        }
        SagaReplyMessage timeoutMessage = SagaReplyMessage.builder()
                .sagaId(saga.sagaId())
                .orderNo(saga.orderNo())
                .step(saga.currentStep())
                .result(SagaResult.FAILURE)
                .code("SYSTEM_TIMEOUT")
                .build();
        processFailure(saga, timeoutMessage);
    }
}
