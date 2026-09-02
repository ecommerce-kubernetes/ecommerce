package com.example.userservice.outbox.application.service;

import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OutboxQueryServiceTest {

    @InjectMocks
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxRepository repository;

    @Test
    @DisplayName("대기중인 아웃박스 메시지 목록을 조회한다.")
    void getPendingOutbox_whenPendingMessagesExist_thenReturnResults() {
        //given
        OutboxMessage first = OutboxFixtureBuilder.given().build();
        OutboxMessage second = OutboxFixtureBuilder.given().build();
        given(repository.findOutboxMessageByStatus(OutboxStatus.PENDING)).willReturn(List.of(first, second));
        //when
        List<OutboxMessageResult> results = outboxQueryService.getPendingOutbox();
        //then
        assertThat(results).hasSize(2);

        OutboxMessageResult firstResult = results.get(0);
        assertThat(firstResult.id()).isEqualTo(first.getId());
        assertThat(firstResult.topic()).isEqualTo(first.getTopic());
        assertThat(firstResult.routingKey()).isEqualTo(first.getRoutingKey());
        assertThat(firstResult.headers()).isEqualTo(first.getHeaders());
        assertThat(firstResult.payload()).isEqualTo(first.getPayload());

        OutboxMessageResult secondResult = results.get(1);
        assertThat(secondResult.id()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("대기중인 아웃박스 메시지가 없으면 빈 목록을 반환한다.")
    void getPendingOutbox_whenNoPendingMessages_thenReturnEmptyList() {
        //given
        given(repository.findOutboxMessageByStatus(OutboxStatus.PENDING)).willReturn(List.of());
        //when
        List<OutboxMessageResult> results = outboxQueryService.getPendingOutbox();
        //then
        assertThat(results).isEmpty();
    }
}
