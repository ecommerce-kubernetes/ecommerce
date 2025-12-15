package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
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
public class OrderEventSagaListenerTest {

    @InjectMocks
    private OrderEventSagaListener orderEventSagaListener;
    @Mock
    private SagaManager sagaManager;

    @Test
    @DisplayName("")
    void handleOrderCreated() {
        //given
        OrderCreatedEvent.OrderedItem item1 = OrderCreatedEvent.OrderedItem.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        OrderCreatedEvent.OrderedItem item2 = OrderCreatedEvent.OrderedItem.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(1L)
                .userId(1L)
                .couponId(1L)
                .orderedItems(List.of(item1, item2))
                .usedPoint(1000L)
                .build();
        //when
        orderEventSagaListener.handleOrderCreated(orderCreatedEvent);
        //then
        ArgumentCaptor<SagaStartCommand> captor = ArgumentCaptor.forClass(SagaStartCommand.class);
        verify(sagaManager, times(1)).startSaga(captor.capture());

        SagaStartCommand command = captor.getValue();

        assertThat(command)
                .extracting("orderId", "userId", "couponId", "usedPoint")
                .containsExactly(1L, 1L, 1L, 1000L);
        assertThat(command.getDeductProductList())
                .hasSize(2)
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
    }
}
