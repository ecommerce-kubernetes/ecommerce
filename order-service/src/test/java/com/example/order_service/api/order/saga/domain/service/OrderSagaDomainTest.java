package com.example.order_service.api.order.saga.domain.service;

import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderSagaDomainTest extends ExcludeInfraTest {

    @Autowired
    private OrderSagaDomainService orderSagaDomainService;

    @Test
    @DisplayName("Saga 인스턴스를 생성해 저장한다")
    void saveOrderSagaInstance(){
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
        SagaInstanceDto sagaInstanceDto = orderSagaDomainService.saveOrderSagaInstance(1L, payload);
        //then
        assertThat(sagaInstanceDto.getId()).isNotNull();
        assertThat(sagaInstanceDto)
                .extracting(SagaInstanceDto::getOrderId, SagaInstanceDto::getSagaStep, SagaInstanceDto::getSagaStatus, SagaInstanceDto::getFailureReason)
                .containsExactly(1L, SagaStep.PRODUCT.name(), SagaStatus.STARTED.name(), null);
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
}
