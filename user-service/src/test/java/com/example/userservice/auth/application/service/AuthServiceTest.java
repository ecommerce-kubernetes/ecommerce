package com.example.userservice.auth.application.service;

import com.example.userservice.auth.domain.RefreshTokenFixtureBuilder;
import com.example.userservice.auth.exception.AuthErrorCode;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.TokenRepository;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.application.service.dto.TokenData;
import com.example.userservice.auth.application.service.dto.TokenResult;
import com.example.userservice.auth.domain.RefreshToken;
import com.example.userservice.auth.fixture.AuthUserResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private AuthUserPort authUserPort;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("이메일과 비밀번호로 로그인하면 토큰을 발급하고 리프레시 토큰을 저장한다.")
    void login() {
        //given
        AuthUserResult user = AuthUserResultFixture.anAuthUserResult().build();
        TokenData tokenData = TokenData.of("accessToken", "refreshToken", Duration.ofDays(14));

        given(authUserPort.authenticate(anyString(), anyString())).willReturn(user);
        given(jwtProvider.generateTokenData(user)).willReturn(tokenData);
        //when
        TokenResult result = authService.login("la9814@naver.com", "password1234*");
        //then
        assertThat(result.accessToken()).isEqualTo(tokenData.accessToken());
        assertThat(result.refreshToken()).isEqualTo(tokenData.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        then(tokenRepository).should().save(captor.capture());

        RefreshToken savedRefreshToken = captor.getValue();
        assertThat(savedRefreshToken.getUserId()).isEqualTo(user.id());
        assertThat(savedRefreshToken.getToken()).isEqualTo(tokenData.refreshToken());
        assertThat(savedRefreshToken.getTtl()).isEqualTo(tokenData.refreshTokenTtl());
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰을 재발급하고 새 리프레시 토큰을 저장한다.")
    void refresh() {
        //given
        AuthUserResult user = AuthUserResultFixture.anAuthUserResult().build();
        TokenData tokenData = TokenData.of("newAccessToken", "newRefreshToken", Duration.ofDays(14));
        RefreshToken refreshToken = RefreshTokenFixtureBuilder.given().build();

        given(jwtProvider.getUserId(anyString())).willReturn(user.id());
        given(tokenRepository.findByUserId(anyLong())).willReturn(Optional.of(refreshToken));
        given(authUserPort.getUserById(anyLong())).willReturn(user);
        given(jwtProvider.generateTokenData(user)).willReturn(tokenData);
        //when
        TokenResult result = authService.refresh(refreshToken.getToken());
        //then
        assertThat(result.accessToken()).isEqualTo(tokenData.accessToken());
        assertThat(result.refreshToken()).isEqualTo(tokenData.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        then(tokenRepository).should().save(captor.capture());

        RefreshToken savedRefreshToken = captor.getValue();
        assertThat(savedRefreshToken.getUserId()).isEqualTo(user.id());
        assertThat(savedRefreshToken.getToken()).isEqualTo(tokenData.refreshToken());
        assertThat(savedRefreshToken.getTtl()).isEqualTo(tokenData.refreshTokenTtl());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰이면 예외가 전파되고 새 토큰을 저장하지 않는다.")
    void refresh_whenTokenInvalid_thenThrownException() {
        //given
        willThrow(new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID))
                .given(jwtProvider).getUserId(anyString());
        //when
        //then
        assertThatThrownBy(() -> authService.refresh("invalidToken"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);

        then(tokenRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("리프레시 토큰을 찾을 수 없으면 예외가 발생한다.")
    void refresh_whenTokenNotFound_thenThrownException(){
        //given
        AuthUserResult user = AuthUserResultFixture.anAuthUserResult().build();
        given(jwtProvider.getUserId(anyString())).willReturn(user.id());

        given(tokenRepository.findByUserId(anyLong())).willThrow(new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));
        //when
        //then
        assertThatThrownBy(() -> authService.refresh("notFoundToken"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("리프레시 토큰이 동일하지 않으면 토큰을 삭제한 뒤 예외가 발생한다.")
    void refresh_whenTokenNotMatches_thenDeleteTokenAndThrownException(){
        //given
        RefreshToken refreshToken = RefreshTokenFixtureBuilder.given().withToken("not-match-token").build();

        given(jwtProvider.getUserId(anyString())).willReturn(1L);
        given(tokenRepository.findByUserId(anyLong())).willReturn(Optional.of(refreshToken));
        //when
        //then
        assertThatThrownBy(() -> authService.refresh("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_NOT_MATCHES);

        then(tokenRepository).should().deleteByUserId(refreshToken.getUserId());
    }

    @Test
    @DisplayName("로그아웃하면 유저의 리프레시 토큰을 삭제한다.")
    void logout() {
        //given
        Long userId = 1L;
        //when
        authService.logout(userId);
        //then
        then(tokenRepository).should().deleteByUserId(userId);
    }
}
