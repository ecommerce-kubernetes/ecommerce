package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.SagaErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class SagaServiceTest extends ExcludeInfraTest {

    @Autowired
    private SagaService sagaService;
    @Autowired
    private OrderSagaInstanceRepository orderSagaInstanceRepository;
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("Saga 인스턴스를 생성해 저장한다")
    void initialize(){
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
        SagaInstanceDto sagaInstanceDto = sagaService.initialize(ORDER_NO, payload, SagaStep.PRODUCT);
        //then
        assertThat(sagaInstanceDto.getId()).isNotNull();
        assertThat(sagaInstanceDto)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStatus, SagaInstanceDto::getSagaStep, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStatus.STARTED, SagaStep.PRODUCT, null);
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
    void getSagaBySagaId() {
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.getSagaBySagaId(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.STARTED, null);
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.proceedTo(save.getId(), SagaStep.COUPON);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.COUPON, SagaStatus.STARTED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 다음 Saga 진행 인스턴스로 변경할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void proceedTo_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.proceedTo(999L, SagaStep.COUPON))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga 인스턴스를 찾을 수 없을때는 예외를 던진다")
    void getSagaBySaga_Id_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.getSagaBySagaId(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.finish(save.getId());
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.FINISHED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스 Status를 FINISHED로 변경할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void finish_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.finish(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.fail(save.getId(), null);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.FAILED, null);
    }

    @Test
    @DisplayName("Saga 인스턴스를 실패 처리할때 SAGA 인스턴스를 찾을 수 없으면 예외를 던진다")
    void fail_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.fail(999L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        sagaInstance.changeStep(SagaStep.COUPON);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.startCompensation(save.getId(), SagaStep.PRODUCT, "유효하지 않은 쿠폰");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.COMPENSATING, "유효하지 않은 쿠폰");
    }

    @Test
    @DisplayName("보상을 시작하는데 Saga 인스턴스 상태가 STARTED가 아닌경우 인스턴스 상태를 변경하지 않는다")
    void startCompensation_no_started(){
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        sagaInstance.changeStatus(SagaStatus.FINISHED);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.startCompensation(save.getId(), SagaStep.PRODUCT, "timeout");
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.FINISHED, null);
    }

    @Test
    @DisplayName("보상을 시작할때 주문 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void startCompensation_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.startCompensation(1L, SagaStep.PRODUCT, "유효하지 않은 쿠폰"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
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
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        sagaInstance.startCompensation(SagaStep.COUPON, "포인트가 부족합니다");
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.continueCompensation(save.getId(), SagaStep.PRODUCT);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(SagaInstanceDto::getOrderNo, SagaInstanceDto::getSagaStep,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(ORDER_NO, SagaStep.PRODUCT, SagaStatus.COMPENSATING, "포인트가 부족합니다");

    }

    @Test
    @DisplayName("다음 보상을 진행할때 Saga 인스턴스를 찾을 수 없으면 예외를 던진다")
    void continueCompensation_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.continueCompensation(1L, SagaStep.PRODUCT))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga 인스턴스중 STARTED 이면서 시작시간이 timeout 시간 이전인 SagaInstance를 조회한다")
    void getTimeouts(){
        //given
        String orderNo1 = ORDER_NO + "1";
        String orderNo2 = ORDER_NO + "2";
        String orderNo3 = ORDER_NO + "3";
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance1 = OrderSagaInstance.create(orderNo1, payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance1, "startedAt", LocalDateTime.of(2025,12,22, 23,59,59));
        OrderSagaInstance sagaInstance2 = OrderSagaInstance.create(orderNo2, payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance2, "startedAt", LocalDateTime.of(2025,12,23, 0, 0, 30));
        sagaInstance2.changeStatus(SagaStatus.COMPENSATING);
        OrderSagaInstance sagaInstance3 = OrderSagaInstance.create(orderNo3, payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance3, "startedAt", LocalDateTime.of(2025,12,23,0,0,30));
        OrderSagaInstance save1 = orderSagaInstanceRepository.save(sagaInstance1);
        OrderSagaInstance save2 = orderSagaInstanceRepository.save(sagaInstance2);
        OrderSagaInstance save3 = orderSagaInstanceRepository.save(sagaInstance3);
        //when
        List<SagaInstanceDto> timeouts = sagaService.getTimeouts(LocalDateTime.of(2025, 12, 23, 0, 0, 0));
        //then
        assertThat(timeouts).hasSize(1)
                .extracting(SagaInstanceDto::getId, SagaInstanceDto::getSagaStatus)
                .containsExactly(
                        tuple(save1.getId(), save1.getSagaStatus())
                );
    }

    @Test
    @DisplayName("주문 아이디로 사가 인스턴스를 조회한다")
    void getSagaByOrderNo(){
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        SagaInstanceDto result = sagaService.getSagaByOrderNo(ORDER_NO);
        //then
        assertThat(result)
                .extracting(SagaInstanceDto::getId, SagaInstanceDto::getOrderNo,
                        SagaInstanceDto::getSagaStatus, SagaInstanceDto::getSagaStep)
                .containsExactly(save.getId(), ORDER_NO, SagaStatus.STARTED, SagaStep.PRODUCT);
    }

    @Test
    @DisplayName("주문 아이디로 사가 인스턴스를 조회할때 찾을 수 없는 경우 예외를 던진다")
    void getSagaByOrderNo_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> sagaService.getSagaByOrderNo(ORDER_NO))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_NOT_FOUND);
    }
}
