package com.example.userservice.outbox.adapter.out.persistence;

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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OutboxPersistenceAdapterTest {

    @InjectMocks
    private OutboxPersistenceAdapter outboxPersistenceAdapter;

    @Mock
    private OutboxJpaRepository outboxJpaRepository;

    @Test
    @DisplayName("상태로 아웃박스 메시지 목록 조회를 위임한다.")
    void findOutboxMessageByStatus() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        given(outboxJpaRepository.findOutboxMessageByStatus(OutboxStatus.PENDING)).willReturn(List.of(outboxMessage));
        //when
        List<OutboxMessage> result = outboxPersistenceAdapter.findOutboxMessageByStatus(OutboxStatus.PENDING);
        //then
        assertThat(result).containsExactly(outboxMessage);
        then(outboxJpaRepository).should().findOutboxMessageByStatus(OutboxStatus.PENDING);
    }
}
