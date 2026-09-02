package com.example.userservice.outbox.application.service;

import com.example.userservice.outbox.application.port.OutboxMessageSendPort;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OutboxMessagePublisherTest {

    @InjectMocks
    private OutboxMessagePublisher outboxMessagePublisher;

    @Mock
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxCommandService outboxCommandService;

    @Mock
    private OutboxMessageSendPort outboxMessageSendPort;

    @Test
    @DisplayName("대기중인 메시지가 없으면 아무 것도 하지 않는다.")
    void sendPendingMessage_whenNoPendingMessages_thenDoNothing() {
        //given
        given(outboxQueryService.getPendingOutbox()).willReturn(List.of());
        //when
        outboxMessagePublisher.sendPendingMessage();
        //then
        then(outboxMessageSendPort).should(never()).send(any());
        then(outboxCommandService).should(never()).changeSent(any());
    }

    @Test
    @DisplayName("대기중인 메시지를 모두 발행하고 SENT로 변경한다.")
    void sendPendingMessage_whenPendingMessagesExist_thenSendAndChangeSentForEach() {
        //given
        OutboxMessageResult first = aResult();
        OutboxMessageResult second = aResult();
        given(outboxQueryService.getPendingOutbox()).willReturn(List.of(first, second));
        //when
        outboxMessagePublisher.sendPendingMessage();
        //then
        then(outboxMessageSendPort).should(times(1)).send(first);
        then(outboxMessageSendPort).should(times(1)).send(second);
        then(outboxCommandService).should(times(1)).changeSent(first.id());
        then(outboxCommandService).should(times(1)).changeSent(second.id());
    }

    @Test
    @DisplayName("한 메시지 발행에 실패해도 다음 메시지는 계속 처리한다.")
    void sendPendingMessage_whenSendFails_thenContinueWithNextMessage() {
        //given
        OutboxMessageResult failing = aResult();
        OutboxMessageResult succeeding = aResult();
        given(outboxQueryService.getPendingOutbox()).willReturn(List.of(failing, succeeding));
        willThrow(new RuntimeException("카프카 발행 실패")).given(outboxMessageSendPort).send(failing);
        //when
        outboxMessagePublisher.sendPendingMessage();
        //then
        then(outboxCommandService).should(never()).changeSent(eq(failing.id()));
        then(outboxMessageSendPort).should(times(1)).send(succeeding);
        then(outboxCommandService).should(times(1)).changeSent(succeeding.id());
    }

    @Test
    @DisplayName("SENT 상태 변경에 실패해도 다음 메시지는 계속 처리한다.")
    void sendPendingMessage_whenChangeSentFails_thenContinueWithNextMessage() {
        //given
        OutboxMessageResult failing = aResult();
        OutboxMessageResult succeeding = aResult();
        given(outboxQueryService.getPendingOutbox()).willReturn(List.of(failing, succeeding));
        willThrow(new RuntimeException("상태 변경 실패")).given(outboxCommandService).changeSent(failing.id());
        //when
        outboxMessagePublisher.sendPendingMessage();
        //then
        then(outboxMessageSendPort).should(times(1)).send(failing);
        then(outboxMessageSendPort).should(times(1)).send(succeeding);
        then(outboxCommandService).should(times(1)).changeSent(succeeding.id());
    }

    private OutboxMessageResult aResult() {
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        return OutboxMessageResult.from(outboxMessage);
    }
}
