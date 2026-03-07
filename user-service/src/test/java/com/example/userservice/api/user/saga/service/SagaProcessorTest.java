package com.example.userservice.api.user.saga.service;

import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.user.saga.producer.SagaEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SagaProcessorTest {

    @InjectMocks
    private SagaProcessor sagaProcessor;

    @Mock
    private UserSagaCommandExecutor executor;
    @Mock
    private SagaEventProducer sagaEventProducer;

    @Test
    @DisplayName("유저 포인트 차감 메시지 수신시 포인트를 차감하고 포인트 차감 성공 메세지를 보낸다")
    void userSagaProcess_deduct_stock_success(){
        //given
        UserSagaCommand command = UserSagaCommand.of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(false);
        //when
        sagaProcessor.userSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaSuccess(anyLong(), anyString());
    }

    @Test
    @DisplayName("유저 포인트 차감 메시지 수신 후 포인트 차감이 실패한 경우 실패 메시지를 보낸다")
    void userSagaProcess_insufficient_stock(){
        //given
        UserSagaCommand command = UserSagaCommand.of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
        willThrow(new BusinessException(UserErrorCode.INSUFFICIENT_POINT))
                .given(executor).processSagaCommand(command);
        //when
        sagaProcessor.userSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaFailure(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("유저 포인트 차감 메시지 수신 후 예외가 발생한 경우 실패 메시지를 보낸다")
    void userSagaProcess_deduct_stock_exception(){
        //given
        UserSagaCommand command = UserSagaCommand.of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
        willThrow(new RuntimeException())
                .given(executor).processSagaCommand(command);
        //when
        sagaProcessor.userSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaFailure(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("이미 처리된 SAGA를 중복 수신시 성공 이벤트를 재발행한다")
    void userSagaProcess_duplicate_event(){
        //given
        UserSagaCommand command = UserSagaCommand.of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(true);
        //when
        sagaProcessor.userSagaProcess(command);
        //then
        verify(sagaEventProducer, times(1)).sendSagaSuccess(anyLong(), anyString());
    }

    @Test
    @DisplayName("포인트 복구 메시지 수신 후 포인트 복구를 진행한다")
    void userSagaProcess_refund_point(){
        //given
        UserSagaCommand command = UserSagaCommand.of(UserCommandType.REFUND_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
        given(executor.processSagaCommand(command)).willReturn(false);
        //when
        sagaProcessor.userSagaProcess(command);
        //then
        verify(executor, times(1)).processSagaCommand(command);
    }
}
