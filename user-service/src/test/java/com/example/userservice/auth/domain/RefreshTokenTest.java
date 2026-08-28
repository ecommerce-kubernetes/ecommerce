package com.example.userservice.auth.domain;

import com.example.userservice.auth.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RefreshTokenTest {

    @Test
    @DisplayName("리프레시 토큰을 생성한다.")
    void create() {
        //given
        CreateRefreshTokenContext context = createContext().build();
        //when
        RefreshToken refreshToken = RefreshToken.create(context);
        //then
        assertThat(refreshToken.getUserId()).isEqualTo(1L);
        assertThat(refreshToken.getToken()).isEqualTo("tokenString");
        assertThat(refreshToken.getTtl()).isEqualTo(Duration.ofDays(7));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("리프레시 토큰 생성시 누락된 필드값이 있으면 예외가 발생한다")
    @MethodSource("provideMissingFieldsContext")
    void create_whenMissingField_thenThrownException(String description, CreateRefreshTokenContext context, String errorMessage) {
        //given
        //when
        //then
        assertThatThrownBy(() -> RefreshToken.create(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("토큰이 동일하면 true를 반환한다.")
    void isMatches(){
        //given
        String tokenString = "refresh-token";
        RefreshToken token = RefreshTokenFixtureBuilder.given().withToken(tokenString).build();
        //when
        boolean isMatches = token.isMatches(tokenString);
        //then
        assertThat(isMatches).isTrue();
    }

    @Test
    @DisplayName("토큰이 동일하지 않으면 false를 반환한다.")
    void validateToken_whenTokenIsNotEqual_thenReturnFalse(){
        //given
        String tokenString = "refresh-token";
        RefreshToken token = RefreshTokenFixtureBuilder.given().withToken(tokenString).build();
        //when
        boolean isMatches = token.isMatches("not-match-token");
        //then
        assertThat(isMatches).isFalse();
    }

    static Stream<Arguments> provideMissingFieldsContext () {
        return Stream.of(
                Arguments.of(
                    "userId가 누락되면 예외가 발생한다.",
                        createContext().userId(null).build(),
                        "리프레시 토큰 생성시 유저 아이디는 필수입니다."
                ),
                Arguments.of(
                        "token이 누락되면 예외가 발생한다.",
                        createContext().token(null).build(),
                        "리프레시 토큰 생성시 토큰 값은 필수입니다."
                ),
                Arguments.of(
                        "token이 빈 문자열이면 예외가 발생한다.",
                        createContext().token("").build(),
                        "리프레시 토큰 생성시 토큰 값은 필수입니다."
                ),
                Arguments.of(
                        "ttl이 누락되면 예외가 발생한다.",
                        createContext().ttl(null).build(),
                        "리프레시 토큰 생성시 만료 기간은 필수입니다."
                )
        );
    }

    static CreateRefreshTokenContext.CreateRefreshTokenContextBuilder createContext() {
        return CreateRefreshTokenContext.builder()
                .userId(1L)
                .token("tokenString")
                .ttl(Duration.ofDays(7));
    }
}
