package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCleanupProcessorTest {

    @InjectMocks
    private OrderCleanupProcessor orderCleanupProcessor;

    @Mock
    private OrderQueryService queryService;
    @Mock
    private OrderCommandService commandService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("타임아웃된 대기 주문을 실패 처리 한다")
    void cleanupExpiredPendingOrders(){
        //given
        OrderResult.Summary timeoutOrder = Instancio.of(OrderResult.Summary.class)
                .set(field("status"), OrderStatus.PENDING)
                .create();
        given(queryService.getPendingOrdersBefore(any(), anyInt())).willReturn(List.of(timeoutOrder));
        //when
        orderCleanupProcessor.cleanupExpiredPendingOrders();
        //then
        verify(commandService).changeFailed(any(), any());
    }

    @Test
    @DisplayName("타임아웃된 대기 주문이 없으면 아무런 동작도 수행하지 않는다")
    void cleanupExpiredPendingOrders_empty(){
        //given
        given(queryService.getPendingOrdersBefore(any(), anyInt())).willReturn(List.of());
        //when
        orderCleanupProcessor.cleanupExpiredPendingOrders();
        //then
        verify(commandService, never()).changeFailed(any(), anyString());
    }
}