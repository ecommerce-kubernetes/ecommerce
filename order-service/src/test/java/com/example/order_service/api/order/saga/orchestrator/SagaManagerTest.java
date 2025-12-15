package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.infrastructure.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SagaManagerTest {

    @InjectMocks
    private SagaManager sagaManager;

    @Mock
    private SagaEventProducer sagaEventProducer;
    @Mock
    private OrderSagaDomainService orderSagaDomainService;

    @Test
    @DisplayName("")
    void startSaga() {
        //given
        SagaStartCommand.DeductProduct deductProduct1 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        SagaStartCommand.DeductProduct deductProduct2 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();

        SagaStartCommand command = SagaStartCommand.builder()
                .orderId(1L)
                .userId(1L)
                .couponId(1L)
                .deductProductList(List.of(deductProduct1, deductProduct2))
                .usedPoint(1000L)
                .build();
        //when
        sagaManager.startSaga(command);
        //then
        ArgumentCaptor<Long> orderIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Payload> payloadCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(orderSagaDomainService, times(1))
                .saveOrderSagaInstance(orderIdCaptor.capture(), payloadCaptor.capture());

        assertThat(orderIdCaptor.getValue()).isEqualTo(1L);
        assertThat(payloadCaptor.getValue())
                .extracting("userId", "couponId", "useToPoint")
                .containsExactly(1L, 1L, 1000L);

        assertThat(payloadCaptor.getValue().getSagaItems())
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
    }
}
