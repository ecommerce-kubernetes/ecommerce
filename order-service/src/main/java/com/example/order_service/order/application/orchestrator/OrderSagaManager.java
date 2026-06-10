package com.example.order_service.order.application.orchestrator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.saga.OrderSagaService;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.saga.StepResult;
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
    public void startSaga(String orderNo) {
        orderCommandService.changePaid(orderNo);
        OrderResult.Detail order = orderQueryService.getOrder(orderNo);
        SagaPayload payload = createPayload(order);
        OrderSagaCommand.Create command = OrderSagaCommand.Create.of(order.orderNo(), SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        orderSagaService.createSaga(command);
    }

    private SagaPayload createPayload(OrderResult.Detail order) {
        List<Long> itemCouponIds = order.items().stream()
                .map(item -> item.itemCoupon().getCouponId())
                .filter(java.util.Objects::nonNull)
                .toList();
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(order.usedPoints());
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(order.cartCoupon().getCouponId(), itemCouponIds);
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
        OrderSagaResult.Default saga = orderSagaService.getSaga(message.getOrderNo());
        if (saga.currentStep() != message.getStep()) {
            log.warn("Saga 지연/중복 응답 주문:{}, 스텝:{}", message.getOrderNo(), message.getStep());
            return;
        }
        /*
        [NOTE] SAGA 흐름
        정방향 처리 : 재고 차감 -> 쿠폰 무효화 -> 포인트 차감 -> 주문 완료
        롤백 처리 : 쿠폰 복구 -> 재고 복구 -> 주문 실패
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

        //[NOTE] 정방향 처리가 필요 없는 경우 다음 단계로 넘어감
        switch (saga.currentStep()) {
            case INVENTORY_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.process(saga.orderNo(), SagaStep.COUPON_USE_PENDING, historyCommand);
                } else if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.process(saga.orderNo(), SagaStep.POINTS_DEDUCT_PENDING, historyCommand);
                } else {
                    orderCommandService.changeCompleted(saga.orderNo());
                    orderSagaService.complete(saga.orderNo(), historyCommand);
                }
            }

            case INVENTORY_RESTORE_PENDING -> {
                orderCommandService.changeFailed(saga.orderNo(), saga.causeCode());
                orderSagaService.fail(saga.orderNo(), historyCommand);
            }

            case COUPON_USE_PENDING -> {
                if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.process(saga.orderNo(), SagaStep.POINTS_DEDUCT_PENDING, historyCommand);
                } else {
                    orderCommandService.changeCompleted(saga.orderNo());
                    orderSagaService.complete(saga.orderNo(), historyCommand);
                }
            }

            case COUPON_RESTORE_PENDING -> orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);

            case POINTS_DEDUCT_PENDING -> {
                orderCommandService.changeCompleted(saga.orderNo());
                orderSagaService.complete(saga.orderNo(), historyCommand);
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
                orderSagaService.fail(saga.orderNo(), historyCommand);
            }
            case COUPON_USE_PENDING -> orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);
            case POINTS_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.process(saga.orderNo(), SagaStep.COUPON_RESTORE_PENDING, historyCommand);
                } else {
                    orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING, historyCommand);
                }
            }
            case COUPON_RESTORE_PENDING, INVENTORY_RESTORE_PENDING -> log.error("보상 실패 sagaId:{}", saga.sagaId());
        }
    }
}
