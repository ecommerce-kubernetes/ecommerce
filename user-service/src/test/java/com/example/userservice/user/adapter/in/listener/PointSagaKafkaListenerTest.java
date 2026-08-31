package com.example.userservice.user.adapter.in.listener;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.outbox.domain.MessageCommandType;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.application.service.PointSagaProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PointSagaKafkaListenerTest {

    @InjectMocks
    private PointSagaKafkaListener pointSagaKafkaListener;

    @Mock
    private PointSagaProcessor processor;

    @Test
    @DisplayName("포인트 차감 커맨드를 수신하면 deduct를 호출한다.")
    void handlePointMessage_whenDeductPoint_thenCallDeduct() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = PointSagaCommandPayload.builder()
                .executionId(10L)
                .userId(100L)
                .usedPoints(5000L)
                .build();
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, MessageCommandType.DEDUCT_POINT);
        //then
        then(processor).should(times(1)).deduct(sagaId, payload.executionId(), payload.userId(), Money.wons(5000L));
        then(processor).should(never()).refund(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("포인트 환불 커맨드를 수신하면 refund를 호출한다.")
    void handlePointMessage_whenRefundPoint_thenCallRefund() {
        //given
        Long sagaId = 1L;
        PointSagaCommandPayload payload = PointSagaCommandPayload.builder()
                .executionId(10L)
                .userId(100L)
                .usedPoints(3000L)
                .build();
        //when
        pointSagaKafkaListener.handlePointMessage(payload, sagaId, MessageCommandType.REFUND_POINT);
        //then
        then(processor).should(times(1)).refund(sagaId, payload.executionId(), payload.userId(), Money.wons(3000L));
        then(processor).should(never()).deduct(anyLong(), anyLong(), anyLong(), any());
    }
}
