package com.example.userservice.user.adapter.out.persistence;

import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("이메일로 유저 조회를 위임한다.")
    void findByEmail() {
        //given
        User user = UserFixtureBuilder.given().build();
        given(userJpaRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        //when
        Optional<User> result = userPersistenceAdapter.findByEmail(user.getEmail());
        //then
        assertThat(result).contains(user);
        then(userJpaRepository).should().findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("이메일 존재 여부 조회를 위임한다.")
    void existsByEmail() {
        //given
        given(userJpaRepository.existsByEmail("la9814@naver.com")).willReturn(true);
        //when
        boolean exists = userPersistenceAdapter.existsByEmail("la9814@naver.com");
        //then
        assertThat(exists).isTrue();
        then(userJpaRepository).should().existsByEmail("la9814@naver.com");
    }
}
