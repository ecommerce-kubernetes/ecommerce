package com.example.order_service.order.application.service.order;

import com.example.order_service.order.adapter.fixture.OrderResultFixture;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderExpirationProcessorTest {

    @InjectMocks
    private OrderExpirationProcessor expirationService;

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
        LocalDateTime currentTime = LocalDateTime.now();
        given(orderProperties.timeoutMinute()).willReturn(30);

        OrderSummaryResult order1 = OrderResultFixture.anSummaryResult().orderId(1L).build();
        OrderSummaryResult order2 = OrderResultFixture.anSummaryResult().orderId(2L).build();

        given(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(order1, order2));

        // when
        expirationService.processTimeoutOrders(currentTime);

        // then
        verify(orderCommandService, times(1)).changeFailed(1L, "접수 시간 초과");
        verify(orderCommandService, times(1)).changeFailed(2L, "접수 시간 초과");
    }

    @Test
    @DisplayName("타임아웃 대상 주문이 없으면 스킵된다")
    void processTimeoutOrders_whenNotExistTimeoutOrders_thenSkip() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(orderProperties.timeoutMinute()).willReturn(30);
        given(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // when
        expirationService.processTimeoutOrders(currentTime);

        // then
        verify(orderCommandService, never()).changeFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("특정 주문 처리 중 예외가 발생하더라도, 다음 주문은 정상적으로 처리되어야 한다.")
    void processTimeoutOrders_whenThrownExceptionOrder_thenProcessingOtherOrders() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(orderProperties.timeoutMinute()).willReturn(30);

        OrderSummaryResult order1 = OrderResultFixture.anSummaryResult().orderId(1L).build();
        OrderSummaryResult order2 = OrderResultFixture.anSummaryResult().orderId(2L).build();

        given(orderQueryService.getOrdersByPendingAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(order1, order2));

        willThrow(new RuntimeException("DB Connection Timeout"))
                .given(orderCommandService).changeFailed(1L, "접수 시간 초과");

        // when
        expirationService.processTimeoutOrders(currentTime);

        // then
        verify(orderCommandService, times(1)).changeFailed(1L, "접수 시간 초과");
        verify(orderCommandService, times(1)).changeFailed(2L, "접수 시간 초과");
    }
}