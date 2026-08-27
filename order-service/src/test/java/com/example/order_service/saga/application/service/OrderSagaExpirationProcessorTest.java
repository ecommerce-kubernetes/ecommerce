package com.example.order_service.saga.application.service;

import com.example.order_service.common.exception.PortException;
import com.example.order_service.saga.application.service.dto.result.OrderSagaExecutionResult;
import com.example.order_service.saga.config.SagaProperties;
import com.example.order_service.saga.exception.SagaErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaExpirationProcessorTest {

    @InjectMocks
    private OrderSagaExpirationProcessor expirationProcessor;

    @Mock
    private SagaProperties sagaProperties;

    @Mock
    private OrderSagaQueryService orderSagaQueryService;

    @Mock
    private OrderSagaCommandService orderSagaCommandService;

    @Test
    @DisplayName("타임아웃 대상 정방향 대기 사가가 없으면 스킵한다.")
    void processTimeoutForwardPendingSagas_whenNotExistTimeoutExecutions_thenSkip() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutForwardPending()).willReturn(5);
        given(orderSagaQueryService.getForwardPendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());
        //when
        expirationProcessor.processTimeoutForwardPendingSagas(currentTime);
        //then
        verify(orderSagaCommandService, never()).failForward(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("타임아웃 대상 정방향 대기 사가가 존재하면 정방향 처리 타임아웃으로 실패 처리한다.")
    void processTimeoutForwardPendingSagas_whenExistTimeoutExecutions_thenFailForward() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutForwardPending()).willReturn(5);

        OrderSagaExecutionResult execution1 = OrderSagaExecutionResult.builder().sagaId(1L).executionId(10L).build();
        OrderSagaExecutionResult execution2 = OrderSagaExecutionResult.builder().sagaId(2L).executionId(20L).build();

        given(orderSagaQueryService.getForwardPendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(List.of(execution1, execution2));
        //when
        expirationProcessor.processTimeoutForwardPendingSagas(currentTime);
        //then
        verify(orderSagaCommandService).failForward(1L, 10L, "정방향 처리 타임아웃");
        verify(orderSagaCommandService).failForward(2L, 20L, "정방향 처리 타임아웃");
    }

    @Test
    @DisplayName("정방향 대기 처리 중 특정 사가에서 예외가 발생하더라도, 다음 사가는 정상적으로 처리되어야 한다.")
    void processTimeoutForwardPendingSagas_whenThrownExceptionOnOneSaga_thenProcessingOthers() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutForwardPending()).willReturn(5);

        OrderSagaExecutionResult execution1 = OrderSagaExecutionResult.builder().sagaId(1L).executionId(10L).build();
        OrderSagaExecutionResult execution2 = OrderSagaExecutionResult.builder().sagaId(2L).executionId(20L).build();

        given(orderSagaQueryService.getForwardPendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(List.of(execution1, execution2));
        willThrow(new PortException(SagaErrorCode.NOT_FOUND_SAGA))
                .given(orderSagaCommandService).failForward(eq(1L), eq(10L), anyString());
        //when
        expirationProcessor.processTimeoutForwardPendingSagas(currentTime);
        //then
        verify(orderSagaCommandService).failForward(1L, 10L, "정방향 처리 타임아웃");
        verify(orderSagaCommandService).failForward(2L, 20L, "정방향 처리 타임아웃");
    }

    @Test
    @DisplayName("타임아웃 대상 보상 대기 사가가 없으면 스킵한다.")
    void processTimeoutCompensatePendingSagas_whenNotExistTimeoutExecutions_thenSkip() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutCompensatePending()).willReturn(5);
        given(orderSagaQueryService.getCompensatePendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());
        //when
        expirationProcessor.processTimeoutCompensatePendingSagas(currentTime);
        //then
        verify(orderSagaCommandService, never()).retryCompensate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("타임아웃 대상 보상 대기 사가가 존재하면 보상을 재시도한다.")
    void processTimeoutCompensatePendingSagas_whenExistTimeoutExecutions_thenRetryCompensate() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutCompensatePending()).willReturn(5);

        OrderSagaExecutionResult execution1 = OrderSagaExecutionResult.builder().sagaId(1L).executionId(10L).build();
        OrderSagaExecutionResult execution2 = OrderSagaExecutionResult.builder().sagaId(2L).executionId(20L).build();

        given(orderSagaQueryService.getCompensatePendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(List.of(execution1, execution2));
        //when
        expirationProcessor.processTimeoutCompensatePendingSagas(currentTime);
        //then
        verify(orderSagaCommandService).retryCompensate(1L, 10L);
        verify(orderSagaCommandService).retryCompensate(2L, 20L);
    }

    @Test
    @DisplayName("보상 대기 처리 중 특정 사가에서 예외가 발생하더라도, 다음 사가는 정상적으로 처리되어야 한다.")
    void processTimeoutCompensatePendingSagas_whenThrownExceptionOnOneSaga_thenProcessingOthers() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(sagaProperties.timeoutCompensatePending()).willReturn(5);

        OrderSagaExecutionResult execution1 = OrderSagaExecutionResult.builder().sagaId(1L).executionId(10L).build();
        OrderSagaExecutionResult execution2 = OrderSagaExecutionResult.builder().sagaId(2L).executionId(20L).build();

        given(orderSagaQueryService.getCompensatePendingExecutionsBefore(any(LocalDateTime.class)))
                .willReturn(List.of(execution1, execution2));
        willThrow(new RuntimeException("이벤트 발행 실패"))
                .given(orderSagaCommandService).retryCompensate(eq(1L), eq(10L));
        //when
        expirationProcessor.processTimeoutCompensatePendingSagas(currentTime);
        //then
        verify(orderSagaCommandService).retryCompensate(1L, 10L);
        verify(orderSagaCommandService).retryCompensate(2L, 20L);
    }
}
