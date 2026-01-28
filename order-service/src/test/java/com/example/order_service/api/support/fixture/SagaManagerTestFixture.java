package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.facade.event.OrderEventStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.model.vo.Payload.SagaItem;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand.DeductProduct;

import java.time.LocalDateTime;
import java.util.List;

public class SagaManagerTestFixture {
    public static final Long SAGA_ID = 1L;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    public static final Long USER_ID = 1L;
    public static final Long PRODUCT_VARIANT_ID_1 = 10L;
    public static final Long PRODUCT_VARIANT_ID_2 = 20L;
    public static final Long COUPON_ID = 100L;
    public static final Long USE_POINT = 1000L;
    public static final Long NO_POINT = 0L;

    public static SagaStartCommand.SagaStartCommandBuilder anSagaStartCommand(){
        return SagaStartCommand.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .couponId(1L)
                .deductProductList(List.of(anDeductedProduct().build()))
                .usedPoint(1000L);
    }

    public static DeductProduct.DeductProductBuilder anDeductedProduct() {
        return DeductProduct.builder()
                .productVariantId(1L)
                .quantity(1);
    }

    public static SagaInstanceDto.SagaInstanceDtoBuilder anSagaInstanceDto() {
        return SagaInstanceDto.builder()
                .id(1L)
                .orderNo(ORDER_NO)
                .sagaStep(SagaStep.PRODUCT)
                .sagaStatus(SagaStatus.STARTED)
                .payload(anPayload().build())
                .startedAt(LocalDateTime.now());
    }

    public static Payload.PayloadBuilder anPayload() {
        return Payload.builder()
                .userId(1L)
                .sagaItems(List.of(anSagaItem().build()))
                .couponId(1L)
                .useToPoint(1000L);
    }

    public static SagaItem.SagaItemBuilder anSagaItem() {
        return SagaItem.builder()
                .productVariantId(1L)
                .quantity(1);
    }

    public static Payload createPayload(Long couponId, Long usedPoint) {
        return Payload.builder()
                .userId(USER_ID)
                .sagaItems(
                        List.of(SagaItem.builder().productVariantId(PRODUCT_VARIANT_ID_1).quantity(3).build())
                )
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();
    }

    public static Payload createDefaultPayload() {
        return createPayload(COUPON_ID, USE_POINT);
    }

    public static SagaInstanceDto createSagaInstance(SagaStep step, SagaStatus status, Payload payload) {
        return SagaInstanceDto.builder()
                .id(SAGA_ID)
                .orderNo(ORDER_NO)
                .sagaStatus(status)
                .sagaStep(step)
                .payload(payload)
                .build();
    }

    public static SagaInstanceDto createSagaInstanceWithId(Long id, SagaStep step, SagaStatus status, Payload payload) {
        return SagaInstanceDto.builder()
                .id(id)
                .orderNo(ORDER_NO)
                .sagaStatus(status)
                .sagaStep(step)
                .payload(payload)
                .build();
    }

    public static SagaPaymentCommand createPaymentSuccessCommand() {
        return SagaPaymentCommand.builder()
                .orderNo(ORDER_NO)
                .status(OrderEventStatus.SUCCESS)
                .code(null)
                .build();
    }

    public static SagaPaymentCommand createPaymentFailCommand(String failureReason) {
        return SagaPaymentCommand.builder()
                .orderNo(ORDER_NO)
                .status(OrderEventStatus.FAILURE)
                .code(OrderFailureCode.PAYMENT_FAILED)
                .failureReason(failureReason)
                .build();
    }
}
