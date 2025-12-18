package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderSagaDomainServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderSagaDomainService orderSagaDomainService;
    @Autowired
    private OrderSagaInstanceRepository orderSagaInstanceRepository;

    @Test
    @DisplayName("Saga 인스턴스를 생성해 저장한다")
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
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.create(1L, payload);
        //then
        assertThat(sagaInstanceDto.getId()).isNotNull();
        assertThat(sagaInstanceDto)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStatus, SagaInstanceDto::getSagaStep, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStatus.STARTED, SagaStep.PRODUCT, null);
        assertThat(sagaInstanceDto.getPayload())
                .extracting(Payload::getUserId, Payload::getUserId, Payload::getUseToPoint)
                .containsExactlyInAnyOrder(1L, 1L, 1000L);
        assertThat(sagaInstanceDto.getPayload().getSagaItems()).hasSize(2)
                .extracting(Payload.SagaItem::getProductVariantId, Payload.SagaItem::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
    }

    @Test
    @DisplayName("Saga 인스턴스를 조회한다")
    void getSaga() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.getSaga(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.STARTED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 찾을 수 없을때는 예외를 던진다")
    void getSaga_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.getSaga(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스 Step을 Coupon으로 변경한다")
    void nextStepToCoupon() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.nextStepToCoupon(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.COUPON, SagaStatus.STARTED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스 Step을 Coupon으로 변경할때 Saga 인스턴스를 찾을 수 없을때 예외를 던진다")
    void nextStepToCoupon_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.nextStepToCoupon(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스 Step을 User로 변경한다")
    void nextStepToUser() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.nextStepToUser(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.USER, SagaStatus.STARTED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스 Step을 USER로 변경할때 Saga 인스턴스를 찾을 수 없을때 예외를 던진다")
    void nextStepToUser_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.nextStepToUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스 Status를 FINISHED로 변경한다")
    void finish() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.finish(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.FINISHED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스 Status를 FINISHED로 변경할때 Saga 인스턴스를 찾을 수 없을때 예외를 던진다")
    void finish_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.finish(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }
}
