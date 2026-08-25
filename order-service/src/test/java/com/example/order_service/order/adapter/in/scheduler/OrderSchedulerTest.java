package com.example.order_service.order.adapter.in.scheduler;

import com.example.order_service.order.adapter.fixture.OrderResultFixture;
import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.config.OrderProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    @InjectMocks
    private OrderScheduler orderScheduler;

    @Mock
    private OrderProperties orderProperties;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private OrderCommandService orderCommandService;

    @Test
    @DisplayName("타임아웃 대상 주문이 존재하면, 해당 주문들을 실패 처리한다.")
    void processTimeoutOrders_whenExistTimeoutOrders_thenChangeFailed() {
        // given
        when(orderProperties.timeoutMinute()).thenReturn(30);

        OrderSummaryResult order1 = OrderResultFixture.anSummaryResult().orderId(1L).build();
        OrderSummaryResult order2 = OrderResultFixture.anSummaryResult().orderId(2L).build();

        when(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(order1, order2));

        // when
        orderScheduler.processTimeoutOrders();

        // then
        verify(orderCommandService, times(1)).changeFailed(1L, "접수 시간 초과");
        verify(orderCommandService, times(1)).changeFailed(2L, "접수 시간 초과");
    }

    @Test
    @DisplayName("타임아웃 대상 주문이 없으면 스킵된다")
    void processTimeoutOrders_whenNotExistTimeoutOrders_thenSkip() {
        // given
        when(orderProperties.timeoutMinute()).thenReturn(30);
        when(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // when
        orderScheduler.processTimeoutOrders();

        // then
        verify(orderCommandService, never()).changeFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("특정 주문 처리 중 예외가 발생하더라도, 다음 주문은 정상적으로 처리되어야 한다.")
    void processTimeoutOrders_whenThrownExceptionOrder_thenProcessingOtherOrders() {
        // given
        when(orderProperties.timeoutMinute()).thenReturn(30);

        OrderSummaryResult order1 = OrderResultFixture.anSummaryResult().orderId(1L).build();
        OrderSummaryResult order2 = OrderResultFixture.anSummaryResult().orderId(2L).build();

        when(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(order1, order2));

        doThrow(new RuntimeException("DB Connection Timeout"))
                .when(orderCommandService).changeFailed(1L, "접수 시간 초과");

        // when
        orderScheduler.processTimeoutOrders();

        // then
        verify(orderCommandService, times(1)).changeFailed(1L, "접수 시간 초과");
        verify(orderCommandService, times(1)).changeFailed(2L, "접수 시간 초과");
    }
}