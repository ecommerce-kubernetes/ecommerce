package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.application.event.OrderEventStatus;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;

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

    public static SagaStartCommand createStartCommand(){
        SagaStartCommand.DeductProduct item1 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(PRODUCT_VARIANT_ID_1).quantity(3).build();
        SagaStartCommand.DeductProduct item2 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(PRODUCT_VARIANT_ID_2).quantity(5).build();

        return SagaStartCommand.builder()
                .orderNo(ORDER_NO)
                .userId(USER_ID)
                .couponId(COUPON_ID)
                .deductProductList(List.of(item1, item2))
                .usedPoint(USE_POINT)
                .build();
    }

    public static Payload createPayload(Long couponId, Long usedPoint) {
        return Payload.builder()
                .userId(USER_ID)
                .sagaItems(
                        List.of(Payload.SagaItem.builder().productVariantId(PRODUCT_VARIANT_ID_1).quantity(3).build())
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
