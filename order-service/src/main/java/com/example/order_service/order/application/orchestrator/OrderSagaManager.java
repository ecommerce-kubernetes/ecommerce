package com.example.order_service.order.application.orchestrator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.OrderCommandService;
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

    private final OrderCommandService orderCommandService;
    private final OrderSagaService orderSagaService;

    @Transactional
    public void startSaga(String orderNo) {
        OrderResult.Detail order = orderCommandService.changePaid(orderNo);
        SagaPayload payload = createPayload(order);
        OrderSagaCommand.Create command = OrderSagaCommand.Create.of(order.orderNo(), SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        orderSagaService.createSaga(command);
    }

    @Transactional
    public void handleReply(SagaReplyMessage message) {
        OrderSagaResult.Default saga = orderSagaService.getSaga(message.getOrderNo());
        if (saga.currentStep() != message.getStep()) {
            log.warn("Saga 지연/중복 응답 주문:{}, 스텝:{}", message.getOrderNo(), message.getStep());
            return;
        }
        StepResult stepResult = message.getResult() == SagaResult.SUCCESS ? StepResult.COMPLETED : StepResult.FAILED;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(saga.orderNo(), stepResult, saga.currentStep(), message.getCode());
        orderSagaService.recordHistory(command);
        if (message.getResult() == SagaResult.SUCCESS) {
            processSuccess(saga);
        } else {
            processFailure(saga);
        }
    }

    private void processSuccess(OrderSagaResult.Default saga) {
        SagaPayload payload = saga.payload();
        switch (saga.currentStep()) {
            case INVENTORY_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.process(saga.orderNo(), SagaStep.COUPON_USE_PENDING);
                } else if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.process(saga.orderNo(), SagaStep.POINTS_DEDUCT_PENDING);
                } else {
                    orderSagaService.complete(saga.orderNo());
                }
            }
            case COUPON_USE_PENDING -> {
                if (!payload.getPoints().getUsedPoints().equals(Money.ZERO)) {
                    orderSagaService.process(saga.orderNo(), SagaStep.POINTS_DEDUCT_PENDING);
                } else {
                    orderSagaService.complete(saga.orderNo());
                }
            }
            case POINTS_DEDUCT_PENDING -> orderSagaService.complete(saga.orderNo());
            case INVENTORY_RESTORE_PENDING -> orderSagaService.fail(saga.orderNo());
            case COUPON_RESTORE_PENDING -> orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING);
        }
    }

    private void processFailure(OrderSagaResult.Default saga) {
        SagaPayload payload = saga.payload();
        switch (saga.currentStep()) {
            case INVENTORY_DEDUCT_PENDING -> orderSagaService.fail(saga.orderNo());
            case COUPON_USE_PENDING -> orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING);
            case POINTS_DEDUCT_PENDING -> {
                if (payload.getCoupon().getCartCouponId() != null || !payload.getCoupon().getItemCouponIds().isEmpty()) {
                    orderSagaService.process(saga.orderNo(), SagaStep.COUPON_RESTORE_PENDING);
                } else {
                    orderSagaService.process(saga.orderNo(), SagaStep.INVENTORY_RESTORE_PENDING);
                }
            }
            case COUPON_RESTORE_PENDING, INVENTORY_RESTORE_PENDING -> log.error("보상 실패 sagaId:{}", saga.sagaId());
        }
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
}
