package com.example.userservice.auth.domain;

import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("리프레시 토큰을 생성한다.")
    void create() {
        //given
        CreateRefreshTokenContext context = CreateRefreshTokenContext.builder()
                .userId(1L)
                .token("refresh-token")
                .ttl(Duration.ofDays(7))
                .build();
        //when
        RefreshToken refreshToken = RefreshToken.create(context);
        //then
        assertThat(refreshToken.getUserId()).isEqualTo(1L);
        assertThat(refreshToken.getToken()).isEqualTo("refresh-token");
        assertThat(refreshToken.getTtl()).isEqualTo(Duration.ofDays(7));
    }
}
