package com.example.order_service.api.order.saga.domain.model;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderSagaInstanceTest {

    @Test
    @DisplayName("인스턴스 생성시 초기상태는 PRODUCT_STARTED 인 초기 SAGA 인스턴스이다")
    void start(){
        //given
        Payload.SagaItem item1 = Payload.SagaItem.builder().productVariantId(1L)
                .quantity(3).build();
        Payload.SagaItem item2 = Payload.SagaItem.builder().productVariantId(2L)
                .quantity(5).build();
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(item1, item2))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        //when
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getOrderId, OrderSagaInstance::getSagaStep, OrderSagaInstance::getSagaProgress, OrderSagaInstance::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaProgress.STARTED, null);
        assertThat(sagaInstance.getStartedAt())
                .isNotNull();
        assertThat(sagaInstance.getFinishedAt())
                .isNull();
        assertThat(sagaInstance.getPayload())
                .extracting(Payload::getUserId, Payload::getCouponId, Payload::getUseToPoint)
                .containsExactly(1L, 1L, 1000L);
        assertThat(sagaInstance.getPayload().getSagaItems())
                .extracting(Payload.SagaItem::getProductVariantId, Payload.SagaItem::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
    }
}
