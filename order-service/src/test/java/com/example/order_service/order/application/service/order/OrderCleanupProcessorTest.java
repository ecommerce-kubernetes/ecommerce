package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        OrderResultDeprecated.Summary timeoutOrder = Instancio.of(OrderResultDeprecated.Summary.class)
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

    @Test
    @DisplayName("처리 중 하나의 주문에서 예외가 발생해도 다음 주문 처리를 계속 진행한다")
    void cleanupExpiredPendingOrders_continueOnException() {
        // given
        OrderResultDeprecated.Summary order1 = Instancio.of(OrderResultDeprecated.Summary.class)
                .set(field("orderNo"), "ORD-FAIL-123")
                .create();
        OrderResultDeprecated.Summary order2 = Instancio.of(OrderResultDeprecated.Summary.class)
                .set(field("orderNo"), "ORD-SUCCESS-456")
                .create();

        given(queryService.getPendingOrdersBefore(any(), anyInt())).willReturn(List.of(order1, order2));

        doThrow(new RuntimeException("DB Connection Timeout"))
                .when(commandService).changeFailed(eq("ORD-FAIL-123"), anyString());

        // when
        orderCleanupProcessor.cleanupExpiredPendingOrders();

        // then
        verify(commandService).changeFailed(eq("ORD-FAIL-123"), eq("SYSTEM_TIMEOUT"));
        verify(commandService).changeFailed(eq("ORD-SUCCESS-456"), eq("SYSTEM_TIMEOUT"));
    }

    @Test
    @DisplayName("스레드 인터럽트 발생 시 조기 종료하고 인터럽트 상태를 복구한다 (Graceful Shutdown)")
    void cleanupExpiredPendingOrders_interrupted() {
        // given
        OrderResultDeprecated.Summary order1 = Instancio.of(OrderResultDeprecated.Summary.class).create();
        OrderResultDeprecated.Summary order2 = Instancio.of(OrderResultDeprecated.Summary.class).create();
        given(queryService.getPendingOrdersBefore(any(), anyInt())).willReturn(List.of(order1, order2));
        Thread.currentThread().interrupt();

        // when
        orderCleanupProcessor.cleanupExpiredPendingOrders();

        // then
        verify(commandService).changeFailed(eq(order1.orderNo()), eq("SYSTEM_TIMEOUT"));
        verify(commandService, never()).changeFailed(eq(order2.orderNo()), anyString());
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        boolean interrupted = Thread.interrupted();
    }
}