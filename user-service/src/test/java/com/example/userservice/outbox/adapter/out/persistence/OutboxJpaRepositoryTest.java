package com.example.userservice.outbox.adapter.out.persistence;

import com.example.userservice.common.config.JpaAuditingConfig;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class OutboxJpaRepositoryTest {

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("PENDING 상태이면서 기준 시각 이전에 생성된 아웃박스 메시지만 조회한다.")
    void findOutboxMessageByStatusAndCreatedAtBefore() {
        //given
        OutboxMessage oldPending = OutboxFixtureBuilder.given().build();
        OutboxMessage recentPending = OutboxFixtureBuilder.given().build();
        OutboxMessage oldSent = OutboxFixtureBuilder.given().build();
        oldSent.sent();

        entityManager.persist(oldPending);
        entityManager.persist(recentPending);
        entityManager.persist(oldSent);
        entityManager.flush();

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        setCreatedAt(oldPending, threshold.minusMinutes(1));
        setCreatedAt(oldSent, threshold.minusMinutes(1));
        entityManager.flush();
        entityManager.clear();
        //when
        List<OutboxMessage> result = outboxJpaRepository.findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus.PENDING, threshold);
        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(oldPending.getId());
    }

    @Test
    @DisplayName("조건에 맞는 아웃박스 메시지가 없으면 빈 목록을 반환한다.")
    void findOutboxMessageByStatusAndCreatedAtBefore_whenNoneMatch_thenReturnsEmptyList() {
        //given
        OutboxMessage recentPending = OutboxFixtureBuilder.given().build();
        entityManager.persist(recentPending);
        entityManager.flush();
        entityManager.clear();
        //when
        List<OutboxMessage> result = outboxJpaRepository
                .findOutboxMessageByStatusAndCreatedAtBefore(OutboxStatus.PENDING, LocalDateTime.now().minusMinutes(5));
        //then
        assertThat(result).isEmpty();
    }

    private void setCreatedAt(OutboxMessage outboxMessage, LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createQuery("update OutboxMessage o set o.createdAt = :createdAt where o.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", outboxMessage.getId())
                .executeUpdate();
    }
}
