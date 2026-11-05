package com.example.userservice.user.adapter.out.persistence;

import com.example.userservice.user.domain.PointHistory;
import com.example.userservice.user.domain.PointHistoryFixtureBuilder;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PointHistoryJpaRepositoryTest {

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("referenceId로 저장된 포인트 기록이 있으면 true를 반환한다.")
    void existsByReferenceId_whenExists_thenReturnsTrue() {
        //given
        User user = UserFixtureBuilder.given().build();
        entityManager.persist(user);
        PointHistory history = PointHistoryFixtureBuilder.given()
                .withReferenceId(1L)
                .withUser(user)
                .buildAddHistory();
        entityManager.persist(history);
        entityManager.flush();
        //when
        boolean exists = pointHistoryJpaRepository.existsByReferenceId(1L);
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("referenceId로 저장된 포인트 기록이 없으면 false를 반환한다.")
    void existsByReferenceId_whenNotExists_thenReturnsFalse() {
        //given
        //when
        boolean exists = pointHistoryJpaRepository.existsByReferenceId(999L);
        //then
        assertThat(exists).isFalse();
    }
}
