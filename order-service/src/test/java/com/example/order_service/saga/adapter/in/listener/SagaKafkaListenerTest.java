package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyMessagePayload;
import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyResult;
import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyType;
import com.example.order_service.saga.application.service.OrderSagaCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SagaKafkaListenerTest {

    @InjectMocks
    private SagaKafkaListener sagaKafkaListener;

    @Mock
    private OrderSagaCommandService orderSagaCommandService;
    
    @Test
    @DisplayName("정방향 스텝 성공 메시지 수신시 completeForward를 호출한다.")
    void handleReplyMessage_whenForwardSuccess_thenCallCompleteForward() {
        //given
        Long sagaId = 1L;
        Long executionId = 10L;
        SagaReplyType replyType = SagaReplyType.FORWARD;

        SagaReplyMessagePayload payload = SagaReplyMessagePayload
                .builder()
                .executionId(executionId)
                .result(SagaReplyResult.SUCCESS)
                .build();
        //when
        sagaKafkaListener.handleReplyMessage(payload, sagaId, replyType);
        //then
        then(orderSagaCommandService).should(times(1)).completeForward(anyLong(), anyLong());
    }

    @Test
    @DisplayName("정방향 스텝 실패 메시지를 수신하면 failForward를 호출한다.")
    void handleReplyMessage_whenForwardFail_thenCallFailForward() {
        // given
        Long sagaId = 1L;
        Long executionId = 10L;
        SagaReplyType replyType = SagaReplyType.FORWARD;

        SagaReplyMessagePayload payload = SagaReplyMessagePayload
                .builder()
                .executionId(executionId)
                .result(SagaReplyResult.FAIL)
                .failureReason("실패 이유")
                .build();

        // when
        sagaKafkaListener.handleReplyMessage(payload, sagaId, replyType);

        // then
        then(orderSagaCommandService).should(times(1)).failForward(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("보상 스텝 성공 메시지를 수신하면 completeCompensate를 호출한다.")
    void handleReplyMessage_whenCompensateSuccess_thenCallCompleteCompensate() {
        // given
        Long sagaId = 1L;
        Long executionId = 10L;
        SagaReplyType replyType = SagaReplyType.COMPENSATE;

        SagaReplyMessagePayload payload = SagaReplyMessagePayload
                .builder()
                .executionId(executionId)
                .result(SagaReplyResult.SUCCESS)
                .build();

        // when
        sagaKafkaListener.handleReplyMessage(payload, sagaId, replyType);

        // then
        then(orderSagaCommandService).should(times(1)).completeCompensate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("보상 스텝 실패 메시지를 수신하면 failCompensate를 호출한다.")
    void handleReplyMessage_whenCompensateFail_thenCallFailCompensate() {
        // given
        Long sagaId = 1L;
        Long executionId = 10L;
        SagaReplyType replyType = SagaReplyType.COMPENSATE;

        SagaReplyMessagePayload payload = SagaReplyMessagePayload
                .builder()
                .executionId(executionId)
                .result(SagaReplyResult.FAIL)
                .failureReason("보상 실패")
                .build();

        // when
        sagaKafkaListener.handleReplyMessage(payload, sagaId, replyType);

        // then
        then(orderSagaCommandService).should(times(1)).failCompensate(anyLong(), anyLong());
    }
}