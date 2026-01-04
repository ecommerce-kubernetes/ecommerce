package com.example.order_service.api.order.saga.domain.repository;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
public class OrderSagaInstanceRepositoryTest extends ExcludeInfraTest {
    @Autowired
    private OrderSagaInstanceRepository orderSagaInstanceRepository;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("시작시간이 입력받은 시작시간 이전이면서 상태가 입력받은 상태와 동일한 SagaInstance를 조회한다")
    void findByStartedAtBeforeAndSagaStatus(){
        //given
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance1 = OrderSagaInstance.create(ORDER_NO+"1", payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance1, "startedAt", LocalDateTime.of(2025,12,22, 23,59,59));
        OrderSagaInstance sagaInstance2 = OrderSagaInstance.create(ORDER_NO+"2", payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance2, "startedAt", LocalDateTime.of(2025,12,23, 0, 0, 30));
        sagaInstance2.changeStatus(SagaStatus.COMPENSATING);
        OrderSagaInstance sagaInstance3 = OrderSagaInstance.create(ORDER_NO+"3", payload, SagaStep.PRODUCT);
        ReflectionTestUtils.setField(sagaInstance3, "startedAt", LocalDateTime.of(2025,12,23,0,0,30));
        OrderSagaInstance save1 = orderSagaInstanceRepository.save(sagaInstance1);
        OrderSagaInstance save2 = orderSagaInstanceRepository.save(sagaInstance2);
        OrderSagaInstance save3 = orderSagaInstanceRepository.save(sagaInstance3);
        //when
        List<OrderSagaInstance> findOrderInstances = orderSagaInstanceRepository.findByStartedAtBeforeAndSagaStatus(LocalDateTime.of(2025, 12, 23, 0, 0, 0), SagaStatus.STARTED);
        //then
        assertThat(findOrderInstances).hasSize(1)
                .extracting(OrderSagaInstance::getId, OrderSagaInstance::getSagaStatus)
                .containsExactly(
                        tuple(save1.getId(), save1.getSagaStatus())
                );
    }

    @Test
    @DisplayName("주문 아이디로 사가 인스턴스를 조회한다")
    void findByOrderId(){
        //given
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        OrderSagaInstance sagaInstance = OrderSagaInstance.create(ORDER_NO, payload, SagaStep.PRODUCT);
        OrderSagaInstance save = orderSagaInstanceRepository.save(sagaInstance);
        //when
        Optional<OrderSagaInstance> findSaga = orderSagaInstanceRepository.findByOrderId(ORDER_NO);
        //then
        assertThat(findSaga).isNotEmpty();
        assertThat(findSaga.get())
                .extracting(OrderSagaInstance::getId, OrderSagaInstance::getOrderNo, OrderSagaInstance::getSagaStatus,
                        OrderSagaInstance::getSagaStep)
                .containsExactly(save.getId(), ORDER_NO, SagaStatus.STARTED,
                        SagaStep.PRODUCT);
    }
}
