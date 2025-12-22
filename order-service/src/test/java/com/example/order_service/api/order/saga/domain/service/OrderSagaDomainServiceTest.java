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
    @DisplayName("Saga 인스턴스를 다음 Saga 진행 인스턴스로 변경한다")
    void proceedTo(){
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
        SagaInstanceDto result = orderSagaDomainService.proceedTo(save.getId(), SagaStep.COUPON);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.COUPON, SagaStatus.STARTED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 다음 Saga 진행 인스턴스로 변경할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void proceedTo_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.proceedTo(999L, SagaStep.COUPON))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스를 다음 Saga 보상 인스턴스로 변경한다")
    void compensateTo(){
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
        SagaInstanceDto result = orderSagaDomainService.compensateTo(save.getId(), SagaStep.COUPON);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.COUPON, SagaStatus.COMPENSATING, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 다음 Saga 보상 인스턴스로 변경할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void compensateTo_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.compensateTo(999L, SagaStep.COUPON))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
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
    @DisplayName("Saga 인스턴스 Status를 FINISHED로 변경할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void finish_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.finish(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스를 실패 처리한다")
    void fail(){
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
        SagaInstanceDto result = orderSagaDomainService.fail(save.getId(), null);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.FAILED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 실패 처리할때 SAGA 인스턴스를 찾을 수 없으면 예외를 던진다")
    void fail_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.fail(999L, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("Saga 인스턴스를 saga 중지로 변경한다")
    void abort(){
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
        SagaInstanceDto result = orderSagaDomainService.abort(save.getId(), "OUT_OF_STOCK");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.ABORTED, "OUT_OF_STOCK");
    }

    @Test
    @DisplayName("Saga 인스턴스를 saga 중지 상태로 변경할 때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void abort_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.abort(999L, "OUT_OF_STOCK"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("보상을 시작하기 위해 SagaInstance를 변경한다")
    void startCompensation() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        sagaInstance.changeStep(SagaStep.COUPON);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.startCompensation(save.getId(), SagaStep.PRODUCT, "유효하지 않은 쿠폰");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.COMPENSATING, "유효하지 않은 쿠폰");
    }

    @Test
    @DisplayName("보상을 시작할때 주문 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void startCompensation_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.startCompensation(1L, SagaStep.PRODUCT, "유효하지 않은 쿠폰"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다음 보상을 진행하기 위해 Saga 인스턴스를 변경한다")
    void continueCompensation() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.start(1L, payload);
        sagaInstance.startCompensation(SagaStep.COUPON, "포인트가 부족합니다");
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = orderSagaDomainService.continueCompensation(save.getId(), SagaStep.PRODUCT);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT, SagaStatus.COMPENSATING, "포인트가 부족합니다");

    }

    @Test
    @DisplayName("다음 보상을 진행할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void continueCompensation_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaDomainService.continueCompensation(1L, SagaStep.PRODUCT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문 SAGA 인스턴스를 찾을 수 없습니다");
    }
}
