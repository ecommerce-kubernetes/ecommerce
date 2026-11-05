package com.example.order_service.order.adapter.in.listener;

import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.saga.domain.event.SagaProcessingFailedEvent;
import com.example.order_service.saga.domain.event.SagaSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderSagaEventListenerTest {

    @InjectMocks
    private OrderSagaEventListener listener;

    @Mock
    private OrderCommandService orderCommandService;

    @Test
    @DisplayName("사가 성공 이벤트 수신시 주문을 완료한다.")
    void handleSagaSuccessEvent(){
        //given
        SagaSuccessEvent event = SagaSuccessEvent.builder()
                .orderId(1L)
                .build();
        //when
        listener.handleSagaSuccessEvent(event);
        //then
        verify(orderCommandService).changeCompleted(anyLong());
    }

    @Test
    @DisplayName("사가 진행 실패 이벤트 수신시 주문을 실패 한다.")
    void handleSagaProcessingFailedEvent(){
        //given
        SagaProcessingFailedEvent event = SagaProcessingFailedEvent.builder().orderId(1L)
                .failureReason("재고 부족")
                .build();
        //when
        listener.handleSagaProcessingFailedEvent(event);
        //then
        verify(orderCommandService).changeFailed(anyLong(), anyString());
    }
}