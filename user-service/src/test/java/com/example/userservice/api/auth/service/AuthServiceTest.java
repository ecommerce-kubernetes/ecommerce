package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.domain.model.RefreshToken;
import com.example.userservice.api.auth.domain.repository.RefreshTokenRepository;
import com.example.userservice.api.auth.service.dto.JwtClaims;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.support.ExcludeInfraTest;
import com.example.userservice.api.support.fixture.UserCommandFixture;
import com.example.userservice.api.user.domain.model.Role;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Transactional
class AuthServiceTest extends ExcludeInfraTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private RefreshTokenRepository tokenRepository;
    @Autowired
    private JwtProvider jwtProvider;

    @Value("${token.refresh_expiration_time}")
    private long refreshTtl;

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("회원의 토큰을 발급하고 리프레시 토큰을 저장한다")
        void login(){
            //given
            UserCreateCommand command = UserCommandFixture.anUserCreateCommand().build();
            String encryptPwd = passwordEncoder.encode(command.getPassword());
            userRepository.save(User.createUser(command, encryptPwd));
            //when
            TokenData tokenData = authService.login("la9814@naver.com", "password1234*");
            //then
            assertThat(tokenData).satisfies(
                    token -> assertThat(token.getAccessToken()).isNotNull(),
                    token -> assertThat(token.getRefreshToken()).isNotNull()
            );

            verify(tokenRepository).save(any(RefreshToken.class), anyLong());
        }

        @Test
        @DisplayName("유저를 찾을 수 없으면 로그인할 수 없다")
        void login_user_not_found(){
            //given
            //when
            //then
            assertThatThrownBy(() -> authService.login("test@email.com", "password"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("비밀번호가 틀리면 로그인할 수 없다")
        void login_user_password_not_match(){
            //given
            UserCreateCommand command = UserCommandFixture.anUserCreateCommand().build();
            String encryptPwd = passwordEncoder.encode(command.getPassword());
            userRepository.save(User.createUser(command, encryptPwd));
            //when
            //then
            String email = command.getEmail();
            assertThatThrownBy(() -> authService.login(email, "invalidPassword"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    @Nested
    @DisplayName("토큰 리프레시")
    class Refresh {

        @Test
        @DisplayName("회원의 토큰을 재발급 하고 리프레시 토큰을 저장한다")
        void refresh() {
            //given
            UserCreateCommand command = UserCommandFixture.anUserCreateCommand().build();
            String encryptPwd = passwordEncoder.encode(command.getPassword());
            User savedUser = userRepository.save(User.createUser(command, encryptPwd));

            JwtClaims jwtClaims = JwtClaims.of(savedUser);
            TokenData initialTokenData = jwtProvider.generateTokenData(jwtClaims);
            String validRefreshToken = initialTokenData.getRefreshToken();
            given(tokenRepository.findById(savedUser.getId()))
                    .willReturn(RefreshToken.create(savedUser.getId(), validRefreshToken));
            //when
            TokenData newTokenData = authService.refresh(validRefreshToken);
            //then
            assertThat(newTokenData).satisfies(
                    token -> assertThat(token.getAccessToken()).isNotNull(),
                    token -> assertThat(token.getRefreshToken()).isNotNull()
            );

            verify(tokenRepository).save(any(RefreshToken.class), anyLong());
        }

        @Test
        @DisplayName("회원의 리프레시 토큰을 찾을 수 없는 경우 예외가 발생한다")
        void refresh_notFoundToken() {
            //given
            UserCreateCommand command = UserCommandFixture.anUserCreateCommand().build();
            String encryptPwd = passwordEncoder.encode(command.getPassword());
            User savedUser = userRepository.save(User.createUser(command, encryptPwd));

            JwtClaims jwtClaims = JwtClaims.of(savedUser);
            TokenData initialTokenData = jwtProvider.generateTokenData(jwtClaims);
            String validRefreshToken = initialTokenData.getRefreshToken();
            given(tokenRepository.findById(savedUser.getId()))
                    .willReturn(null);
            //when
            //then
            assertThatThrownBy(() -> authService.refresh(validRefreshToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        @Test
        @DisplayName("저장된 refresh 토큰과 요청 refresh 토큰이 서로 다른 경우 예외가 발생한다")
        void refresh_notEqualToken() {
            //given
            UserCreateCommand command = UserCommandFixture.anUserCreateCommand().build();
            String encryptPwd = passwordEncoder.encode(command.getPassword());
            User savedUser = userRepository.save(User.createUser(command, encryptPwd));

            JwtClaims jwtClaims = JwtClaims.of(savedUser);
            TokenData initialTokenData = jwtProvider.generateTokenData(jwtClaims);
            String validRefreshToken = initialTokenData.getRefreshToken();
            given(tokenRepository.findById(savedUser.getId()))
                    .willReturn(RefreshToken.create(savedUser.getId(), validRefreshToken));

            JwtClaims otherClaim = JwtClaims.builder()
                    .id(99L)
                    .name("다른 유저")
                    .email("test@naver.com")
                    .role(Role.ROLE_ADMIN).build();
            TokenData otherToken = jwtProvider.generateTokenData(otherClaim);
            //when
            //then
            assertThatThrownBy(() -> authService.refresh(otherToken.getRefreshToken()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {
        @Test
        @DisplayName("로그아웃시 저장된 리프레시 토큰을 삭제한다")
        void logout() {
            //given
            //when
            authService.logout(1L);
            //then
            verify(tokenRepository).deleteById(anyLong());
        }
    }
}
