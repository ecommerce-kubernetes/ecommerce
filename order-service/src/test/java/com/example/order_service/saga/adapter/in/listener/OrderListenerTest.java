package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.event.OrderAcceptedEvent;
import com.example.order_service.saga.application.service.OrderSagaCommandService;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class OrderListenerTest {

    @InjectMocks
    private OrderListener listener;

    @Mock
    private OrderSagaCommandService commandService;

    @Test
    @DisplayName("주문 결제 완료 이벤트 수신시 주문 사가를 생성한다.")
    void handleOrderAcceptEvent() {
        //given
        OrderAcceptedEvent.OrderedItem item = OrderAcceptedEvent.OrderedItem.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        OrderAcceptedEvent event = OrderAcceptedEvent.builder()
                .orderId(1L)
                .userId(1L)
                .items(List.of(item))
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .usedPoints(Money.wons(1000L))
                .build();
        //when
        listener.handleOrderAcceptedEvent(event);
        //then
        verify(commandService).createOrderSaga(any(CreateOrderSagaContext.class));
    }

}