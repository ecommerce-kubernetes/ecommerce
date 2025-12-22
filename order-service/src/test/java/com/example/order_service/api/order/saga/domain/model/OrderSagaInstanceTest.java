package com.example.order_service.api.order.saga.domain.model;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderSagaInstanceTest {

    @Test
    @DisplayName("인스턴스 생성시 초기상태는 STARTED 인 초기 SAGA 인스턴스이다")
    void create(){
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getOrderId, OrderSagaInstance::getSagaStep, OrderSagaInstance::getSagaStatus, OrderSagaInstance::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.STARTED, null);
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

    @Test
    @DisplayName("sagaStep을 변경한다")
    void changeStep() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        //when
        sagaInstance.changeStep(SagaStep.COUPON);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getOrderId, OrderSagaInstance::getSagaStep, OrderSagaInstance::getSagaStatus, OrderSagaInstance::getFailureReason)
                .containsExactly(1L, SagaStep.COUPON, SagaStatus.STARTED, null);
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
                        tuple(1L, 3)
                );
    }

    @Test
    @DisplayName("SagaStatus를 변경한다")
    void changeStatus() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        //when
        sagaInstance.changeStatus(SagaStatus.FINISHED);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getOrderId, OrderSagaInstance::getSagaStep, OrderSagaInstance::getSagaStatus, OrderSagaInstance::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.FINISHED, null);
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
                        tuple(1L, 3)
                );
    }

    @Test
    @DisplayName("Saga를 보상을 시작하기 위해 Step과 Status를 변경하고 실패 이유를 추가한다")
    void startCompensation() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        sagaInstance.changeStep(SagaStep.COUPON);
        //when
        sagaInstance.startCompensation(SagaStep.PRODUCT, "유효하지 않은 쿠폰");
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getSagaStatus, OrderSagaInstance::getSagaStep, OrderSagaInstance::getFailureReason)
                .containsExactly(SagaStatus.COMPENSATING, SagaStep.PRODUCT, "유효하지 않은 쿠폰");
    }

    @Test
    @DisplayName("다음 보상을 진행하기 위해 SagaStep을 다음 Step으로 변경한다")
    void continueCompensation() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        sagaInstance.startCompensation(SagaStep.COUPON, "포인트가 부족합니다");
        //when
        sagaInstance.continueCompensation(SagaStep.PRODUCT);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getSagaStatus, OrderSagaInstance::getSagaStep, OrderSagaInstance::getFailureReason)
                .containsExactly(SagaStatus.COMPENSATING, SagaStep.PRODUCT, "포인트가 부족합니다");
    }

    @Test
    @DisplayName("Saga 인스턴스를 실패 상태로 변경한다")
    void fail() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        //when
        sagaInstance.fail("재고감소 실패");
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getSagaStatus, OrderSagaInstance::getSagaStep, OrderSagaInstance::getFailureReason)
                .containsExactly(SagaStatus.FAILED, SagaStep.PRODUCT, "재고감소 실패");
        assertThat(sagaInstance.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Saga 인스턴스의 실패 메시지는 최초의 메시지여야 한다")
    void fail_exist_failReason() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        sagaInstance.startCompensation(SagaStep.COUPON, "포인트가 부족합니다");
        //when
        sagaInstance.fail(null);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getSagaStatus, OrderSagaInstance::getSagaStep, OrderSagaInstance::getFailureReason)
                .containsExactly(SagaStatus.FAILED, SagaStep.COUPON, "포인트가 부족합니다");
        assertThat(sagaInstance.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Saga 인스턴스를 다음 단계로 변경")
    void proceedTo() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(1L, payload, SagaStep.PRODUCT);
        //when
        sagaInstance.proceedTo(SagaStep.COUPON);
        //then
        assertThat(sagaInstance)
                .extracting(OrderSagaInstance::getSagaStatus, OrderSagaInstance::getSagaStep, OrderSagaInstance::getFailureReason)
                .containsExactly(SagaStatus.STARTED, SagaStep.COUPON, null);
    }
}
