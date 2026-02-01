package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.domain.repository.RefreshTokenRepository;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.support.ExcludeInfraTest;
import com.example.userservice.api.support.fixture.UserCommandFixture;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class AuthServiceTest extends ExcludeInfraTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private RefreshTokenRepository tokenRepository;

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

            Mockito.verify(tokenRepository).save(1L, tokenData.getRefreshToken(), refreshTtl);
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
            assertThatThrownBy(() -> authService.login(command.getEmail(), "invalidPassword"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
    }
}
