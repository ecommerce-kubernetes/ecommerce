package com.example.userservice.auth.domain.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateRefreshTokenContextTest {

    @Test
    @DisplayName("유저 아이디가 누락되면 예외가 발생한다.")
    void userId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CreateRefreshTokenContext.builder()
                .userId(null)
                .token("refresh-token")
                .ttl(Duration.ofDays(7))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리프레시 토큰 생성시 유저 아이디는 필수이다.");
    }

    @Test
    @DisplayName("토큰 값이 누락되면 예외가 발생한다.")
    void token_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CreateRefreshTokenContext.builder()
                .userId(1L)
                .token(null)
                .ttl(Duration.ofDays(7))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리프레시 토큰 생성시 토큰 값은 필수이다.");
    }

    @Test
    @DisplayName("만료 기간이 누락되면 예외가 발생한다.")
    void expiresAt_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CreateRefreshTokenContext.builder()
                .userId(1L)
                .token("refresh-token")
                .ttl(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리프레시 토큰 생성시 만료 기간은 필수이다.");
    }
}
