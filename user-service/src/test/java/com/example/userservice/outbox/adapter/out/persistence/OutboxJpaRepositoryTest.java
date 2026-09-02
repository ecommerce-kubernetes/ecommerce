package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OutboxJpaRepositoryTest {

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("PENDING 상태인 아웃박스 메시지 목록만 조회한다.")
    void findOutboxMessageByStatus() {
        //given
        OutboxMessage pending = OutboxFixtureBuilder.given().build();
        OutboxMessage sent = OutboxFixtureBuilder.given().build();
        sent.sent();
        entityManager.persist(pending);
        entityManager.persist(sent);
        entityManager.flush();
        entityManager.clear();
        //when
        List<OutboxMessage> result = outboxJpaRepository.findOutboxMessageByStatus(OutboxStatus.PENDING);
        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(pending.getId());
        assertThat(result.getFirst().getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("해당 상태의 아웃박스 메시지가 없으면 빈 목록을 반환한다.")
    void findOutboxMessageByStatus_whenNoneMatch_thenReturnsEmptyList() {
        //given
        OutboxMessage sent = OutboxFixtureBuilder.given().build();
        sent.sent();
        entityManager.persist(sent);
        entityManager.flush();
        entityManager.clear();
        //when
        List<OutboxMessage> result = outboxJpaRepository.findOutboxMessageByStatus(OutboxStatus.PENDING);
        //then
        assertThat(result).isEmpty();
    }
}
