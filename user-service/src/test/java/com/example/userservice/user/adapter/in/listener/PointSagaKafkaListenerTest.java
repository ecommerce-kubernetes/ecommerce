package com.example.userservice.user.adapter.in.listener;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.adapter.in.listener.router.PointSagaCommandRouter;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PointSagaKafkaListenerTest {

    @Mock
    private PointSagaCommandRouter usePointRouter;

    @Mock
    private PointSagaCommandRouter restorePointRouter;

    private PointSagaKafkaListener pointSagaKafkaListener;

    @BeforeEach
    void setUp() {
        pointSagaKafkaListener = new PointSagaKafkaListener(List.of(usePointRouter, restorePointRouter));
    }

    @Test
    @DisplayName("USE_POINT 커맨드를 지원하는 라우터를 찾아 execute를 호출한다.")
    void handlePointMessage_whenUsePoint_thenCallExecuteOnMatchingRouter() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        given(usePointRouter.supports(PointSagaCommand.USE_POINT)).willReturn(true);
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT");
        //then
        then(usePointRouter).should().execute(sagaId, payload);
        then(usePointRouter).should(never()).fail(eq(sagaId), eq(payload), anyString());
        then(restorePointRouter).should(never()).execute(sagaId, payload);
    }

    @Test
    @DisplayName("RESTORE_POINT 커맨드를 지원하는 라우터를 찾아 execute를 호출한다.")
    void handlePointMessage_whenRestorePoint_thenCallExecuteOnMatchingRouter() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        given(restorePointRouter.supports(PointSagaCommand.RESTORE_POINT)).willReturn(true);
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, "RESTORE_POINT");
        //then
        then(restorePointRouter).should().execute(sagaId, payload);
        then(restorePointRouter).should(never()).fail(eq(sagaId), eq(payload), anyString());
        then(usePointRouter).should(never()).execute(sagaId, payload);
    }

    @Test
    @DisplayName("execute 중 BusinessException이 발생하면 잡아서 에러 코드와 함께 fail을 호출한다.")
    void handlePointMessage_whenExecuteFailsWithBusinessException_thenCallFailWithErrorCode() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        given(usePointRouter.supports(PointSagaCommand.USE_POINT)).willReturn(true);
        willThrow(new BusinessException(UserErrorCode.INSUFFICIENT_POINTS)).given(usePointRouter).execute(sagaId, payload);
        //when
        //then
        assertThatCode(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .doesNotThrowAnyException();

        then(usePointRouter).should().fail(sagaId, payload, UserErrorCode.INSUFFICIENT_POINTS.name());
    }

    @Test
    @DisplayName("execute 중 IllegalArgumentException이 발생하면 잡아서 INVALID_INPUT_VALUE로 fail을 호출한다.")
    void handlePointMessage_whenExecuteFailsWithIllegalArgumentException_thenCallFailWithInvalidInputValue() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        given(usePointRouter.supports(PointSagaCommand.USE_POINT)).willReturn(true);
        willThrow(new IllegalArgumentException("잘못된 값")).given(usePointRouter).execute(sagaId, payload);
        //when
        //then
        assertThatCode(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .doesNotThrowAnyException();

        then(usePointRouter).should().fail(sagaId, payload, "INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("BusinessException, IllegalArgumentException이 아닌 예외는 잡지 않고 그대로 전파한다.")
    void handlePointMessage_whenExecuteFailsWithSystemException_thenPropagateException() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        given(usePointRouter.supports(PointSagaCommand.USE_POINT)).willReturn(true);
        RuntimeException systemException = new IllegalStateException("일시적인 시스템 오류");
        willThrow(systemException).given(usePointRouter).execute(sagaId, payload);
        //when
        //then
        assertThatThrownBy(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .isSameAs(systemException);

        then(usePointRouter).should(never()).fail(eq(sagaId), eq(payload), anyString());
    }

    @Test
    @DisplayName("커맨드를 지원하는 라우터가 없으면 예외가 발생한다.")
    void handlePointMessage_whenNoRouterSupportsCommand_thenThrownException() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        //when
        //then
        assertThatThrownBy(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .isInstanceOf(UnsupportedOperationException.class);

        then(usePointRouter).should(never()).execute(eq(sagaId), eq(payload));
        then(restorePointRouter).should(never()).execute(eq(sagaId), eq(payload));
    }

    @Test
    @DisplayName("알 수 없는 커맨드 타입 헤더를 수신하면 예외가 발생한다.")
    void handlePointMessage_whenCommandHeaderIsUnknown_thenThrownException() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        //when
        //then
        assertThatThrownBy(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "UNKNOWN_COMMAND"))
                .isInstanceOf(IllegalArgumentException.class);

        then(usePointRouter).should(never()).execute(eq(sagaId), eq(payload));
        then(restorePointRouter).should(never()).execute(eq(sagaId), eq(payload));
    }

    private PointSagaCommandPayload aPayload() {
        return PointSagaCommandPayload.builder()
                .executionId(10L)
                .userId(100L)
                .usedPoints(5000L)
                .build();
    }
}
