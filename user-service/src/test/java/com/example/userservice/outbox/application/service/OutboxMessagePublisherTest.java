package com.example.userservice.outbox.application.service;

import com.example.userservice.common.properties.OutboxSweepProperties;
import com.example.userservice.outbox.application.port.OutboxMessageSendPort;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OutboxMessagePublisherTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 1, 1, 0, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxCommandService outboxCommandService;

    @Mock
    private OutboxMessageSendPort outboxMessageSendPort;

    @Mock
    private OutboxSweepProperties outboxSweepProperties;

    private OutboxMessagePublisher outboxMessagePublisher;

    @BeforeEach
    void setUp() {
        outboxMessagePublisher = new OutboxMessagePublisher(
                outboxQueryService, outboxCommandService, outboxMessageSendPort, outboxSweepProperties, FIXED_CLOCK);
    }

    @Test
    @DisplayName("PENDING 상태의 메시지를 발행하고 SENT로 변경한다.")
    void publishMessage_whenPending_thenSendAndChangeSent() {
        //given
        OutboxMessageResult result = aResult(OutboxStatus.PENDING);
        given(outboxQueryService.getOutbox(result.id())).willReturn(result);
        //when
        outboxMessagePublisher.publishMessage(result.id());
        //then
        then(outboxMessageSendPort).should().send(result);
        then(outboxCommandService).should().changeSent(result.id());
    }

    @Test
    @DisplayName("PENDING 상태가 아니면 발행하지 않는다.")
    void publishMessage_whenNotPending_thenDoNothing() {
        //given
        OutboxMessageResult result = aResult(OutboxStatus.SENT);
        given(outboxQueryService.getOutbox(result.id())).willReturn(result);
        //when
        outboxMessagePublisher.publishMessage(result.id());
        //then
        then(outboxMessageSendPort).should(never()).send(any());
        then(outboxCommandService).should(never()).changeSent(any());
    }

    @Test
    @DisplayName("클락과 스윕 설정으로 계산한 기준 시각으로 좀비 메시지를 조회한다.")
    void sweepZombieMessages_computesThresholdFromClockAndProperties() {
        //given
        given(outboxSweepProperties.thresholdSecond()).willReturn(Duration.ofSeconds(30));
        given(outboxQueryService.getZombieOutboxes(any())).willReturn(List.of());
        //when
        outboxMessagePublisher.sweepZombieMessages();
        //then
        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(outboxQueryService).should().getZombieOutboxes(thresholdCaptor.capture());
        assertThat(thresholdCaptor.getValue()).isEqualTo(FIXED_NOW.minusSeconds(30));
    }

    @Test
    @DisplayName("좀비 메시지가 없으면 아무 것도 하지 않는다.")
    void sweepZombieMessages_whenNoZombieMessages_thenDoNothing() {
        //given
        given(outboxSweepProperties.thresholdSecond()).willReturn(Duration.ofSeconds(30));
        given(outboxQueryService.getZombieOutboxes(any())).willReturn(List.of());
        //when
        outboxMessagePublisher.sweepZombieMessages();
        //then
        then(outboxMessageSendPort).should(never()).send(any());
        then(outboxCommandService).should(never()).changeSent(any());
    }

    @Test
    @DisplayName("좀비 메시지를 모두 발행하고 SENT로 변경한다.")
    void sweepZombieMessages_whenZombieMessagesExist_thenSendAndChangeSentForEach() {
        //given
        given(outboxSweepProperties.thresholdSecond()).willReturn(Duration.ofSeconds(30));
        OutboxMessageResult first = aResult(OutboxStatus.PENDING);
        OutboxMessageResult second = aResult(OutboxStatus.PENDING);
        given(outboxQueryService.getZombieOutboxes(any())).willReturn(List.of(first, second));
        //when
        outboxMessagePublisher.sweepZombieMessages();
        //then
        then(outboxMessageSendPort).should(times(1)).send(first);
        then(outboxMessageSendPort).should(times(1)).send(second);
        then(outboxCommandService).should(times(1)).changeSent(first.id());
        then(outboxCommandService).should(times(1)).changeSent(second.id());
    }

    @Test
    @DisplayName("한 메시지 발행에 실패해도 다음 메시지는 계속 처리한다.")
    void sweepZombieMessages_whenSendFails_thenContinueWithNextMessage() {
        //given
        given(outboxSweepProperties.thresholdSecond()).willReturn(Duration.ofSeconds(30));
        OutboxMessageResult failing = aResult(OutboxStatus.PENDING);
        OutboxMessageResult succeeding = aResult(OutboxStatus.PENDING);
        given(outboxQueryService.getZombieOutboxes(any())).willReturn(List.of(failing, succeeding));
        willThrow(new RuntimeException("카프카 발행 실패")).given(outboxMessageSendPort).send(failing);
        //when
        outboxMessagePublisher.sweepZombieMessages();
        //then
        then(outboxCommandService).should(never()).changeSent(eq(failing.id()));
        then(outboxMessageSendPort).should(times(1)).send(succeeding);
        then(outboxCommandService).should(times(1)).changeSent(succeeding.id());
    }

    @Test
    @DisplayName("SENT 상태 변경에 실패해도 다음 메시지는 계속 처리한다.")
    void sweepZombieMessages_whenChangeSentFails_thenContinueWithNextMessage() {
        //given
        given(outboxSweepProperties.thresholdSecond()).willReturn(Duration.ofSeconds(30));
        OutboxMessageResult failing = aResult(OutboxStatus.PENDING);
        OutboxMessageResult succeeding = aResult(OutboxStatus.PENDING);
        given(outboxQueryService.getZombieOutboxes(any())).willReturn(List.of(failing, succeeding));
        willThrow(new RuntimeException("상태 변경 실패")).given(outboxCommandService).changeSent(failing.id());
        //when
        outboxMessagePublisher.sweepZombieMessages();
        //then
        then(outboxMessageSendPort).should(times(1)).send(failing);
        then(outboxMessageSendPort).should(times(1)).send(succeeding);
        then(outboxCommandService).should(times(1)).changeSent(succeeding.id());
    }

    private OutboxMessageResult aResult(OutboxStatus status) {
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        if (status == OutboxStatus.SENT) {
            outboxMessage.sent();
        }
        return OutboxMessageResult.from(outboxMessage);
    }
}
