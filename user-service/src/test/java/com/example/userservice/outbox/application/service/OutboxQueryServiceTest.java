package com.example.userservice.outbox.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OutboxQueryServiceTest {

    @InjectMocks
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxRepository repository;

    @Test
    @DisplayName("아웃박스 메시지를 단건 조회한다.")
    void getOutbox() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        given(repository.findById(outboxMessage.getId())).willReturn(Optional.of(outboxMessage));
        //when
        OutboxMessageResult result = outboxQueryService.getOutbox(outboxMessage.getId());
        //then
        assertThat(result.id()).isEqualTo(outboxMessage.getId());
        assertThat(result.topic()).isEqualTo(outboxMessage.getTopic());
        assertThat(result.routingKey()).isEqualTo(outboxMessage.getRoutingKey());
        assertThat(result.headers()).isEqualTo(outboxMessage.getHeaders());
        assertThat(result.payload()).isEqualTo(outboxMessage.getPayload());
        assertThat(result.status()).isEqualTo(outboxMessage.getStatus());
    }

    @Test
    @DisplayName("아웃박스 메시지를 찾을 수 없으면 예외가 발생한다.")
    void getOutbox_whenNotFound_thenThrownException() {
        //given
        given(repository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> outboxQueryService.getOutbox(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OutboxErrorCode.OUTBOX_NOT_FOUND);
    }

    @Test
    @DisplayName("기준 시각 이전에 생성된 좀비 아웃박스 메시지 목록을 조회한다.")
    void getZombieOutbox_whenZombieMessagesExist_thenReturnResults() {
        //given
        OutboxMessage first = OutboxFixtureBuilder.given().build();
        OutboxMessage second = OutboxFixtureBuilder.given().build();
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        given(repository.findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus.PENDING, threshold))
                .willReturn(List.of(first, second));
        //when
        List<OutboxMessageResult> results = outboxQueryService.getZombieOutboxes(threshold);
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
    @DisplayName("좀비 아웃박스 메시지가 없으면 빈 목록을 반환한다.")
    void getZombieOutbox_whenNoZombieMessages_thenReturnEmptyList() {
        //given
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        given(repository.findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus.PENDING, threshold))
                .willReturn(List.of());
        //when
        List<OutboxMessageResult> results = outboxQueryService.getZombieOutboxes(threshold);
        //then
        assertThat(results).isEmpty();
    }
}
