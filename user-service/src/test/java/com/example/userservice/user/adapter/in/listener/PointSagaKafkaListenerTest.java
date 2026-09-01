package com.example.userservice.user.adapter.in.listener;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.application.service.PointSagaProcessor;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PointSagaKafkaListenerTest {

    @InjectMocks
    private PointSagaKafkaListener pointSagaKafkaListener;

    @Mock
    private PointSagaProcessor processor;

    @Test
    @DisplayName("USE_POINT 커맨드를 수신하면 deduct를 호출한다.")
    void handlePointMessage_whenUsePoint_thenCallDeduct() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT");
        //then
        then(processor).should().deduct(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
        then(processor).should(never()).refund(anyLong(), anyLong(), anyLong(), any());
        then(processor).should(never()).failDeduct(anyLong(), anyLong(), anyString());
        then(processor).should(never()).failRefund(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("RESTORE_POINT 커맨드를 수신하면 refund를 호출한다.")
    void handlePointMessage_whenRestorePoint_thenCallRefund() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, "RESTORE_POINT");
        //then
        then(processor).should().refund(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
        then(processor).should(never()).deduct(anyLong(), anyLong(), anyLong(), any());
        then(processor).should(never()).failDeduct(anyLong(), anyLong(), anyString());
        then(processor).should(never()).failRefund(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("USE_POINT 처리 중 BusinessException이 발생하면 잡아서 failDeduct를 호출한다.")
    void handlePointMessage_whenDeductFailsWithBusinessException_thenCallFailDeduct() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        BusinessException exception = new BusinessException(UserErrorCode.INSUFFICIENT_POINTS);
        willThrow(exception).given(processor)
                .deduct(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
        //when
        //then
        assertThatCode(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .doesNotThrowAnyException();

        then(processor).should().failDeduct(sagaId, payload.executionId(), UserErrorCode.INSUFFICIENT_POINTS.name());
        then(processor).should(never()).refund(anyLong(), anyLong(), anyLong(), any());
        then(processor).should(never()).failRefund(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("RESTORE_POINT 처리 중 BusinessException이 발생하면 잡아서 failRefund를 호출한다.")
    void handlePointMessage_whenRefundFailsWithBusinessException_thenCallFailRefund() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        BusinessException exception = new BusinessException(UserErrorCode.USER_NOT_FOUND);
        willThrow(exception).given(processor)
                .refund(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
        //when
        //then
        assertThatCode(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "RESTORE_POINT"))
                .doesNotThrowAnyException();

        then(processor).should().failRefund(sagaId, payload.executionId(), UserErrorCode.USER_NOT_FOUND.name());
        then(processor).should(never()).deduct(anyLong(), anyLong(), anyLong(), any());
        then(processor).should(never()).failDeduct(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("BusinessException이 아닌 예외는 잡지 않고 그대로 전파한다.")
    void handlePointMessage_whenDeductFailsWithSystemException_thenPropagateException() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = aPayload();
        RuntimeException systemException = new IllegalStateException("일시적인 시스템 오류");
        willThrow(systemException).given(processor)
                .deduct(sagaId, payload.executionId(), payload.userId(), Money.wons(payload.usedPoints()));
        //when
        //then
        assertThatThrownBy(() -> pointSagaKafkaListener.handlePointMessage(payload, sagaId, "USE_POINT"))
                .isSameAs(systemException);

        then(processor).should(never()).failDeduct(anyLong(), anyLong(), anyString());
    }

    private PointSagaCommandPayload aPayload() {
        return PointSagaCommandPayload.builder()
                .executionId(10L)
                .userId(100L)
                .usedPoints(5000L)
                .build();
    }
}
