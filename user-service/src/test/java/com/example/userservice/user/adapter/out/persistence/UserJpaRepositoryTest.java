package com.example.userservice.user.adapter.out.persistence;

import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("이메일로 유저를 조회한다.")
    void findByEmail_whenExists_thenReturnsUser() {
        //given
        User user = UserFixtureBuilder.given().build();
        entityManager.persist(user);
        entityManager.flush();
        //when
        Optional<User> result = userJpaRepository.findByEmail(user.getEmail());
        //then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다.")
    void findByEmail_whenNotExists_thenReturnsEmpty() {
        //given
        //when
        Optional<User> result = userJpaRepository.findByEmail("notfound@naver.com");
        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일이 존재하면 true를 반환한다.")
    void existsByEmail_whenExists_thenReturnsTrue() {
        //given
        User user = UserFixtureBuilder.given().build();
        entityManager.persist(user);
        entityManager.flush();
        //when
        boolean exists = userJpaRepository.existsByEmail(user.getEmail());
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false를 반환한다.")
    void existsByEmail_whenNotExists_thenReturnsFalse() {
        //given
        //when
        boolean exists = userJpaRepository.existsByEmail("notfound@naver.com");
        //then
        assertThat(exists).isFalse();
    }
}
