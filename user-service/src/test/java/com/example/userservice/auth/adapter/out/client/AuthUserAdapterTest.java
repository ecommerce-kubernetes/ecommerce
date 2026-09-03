package com.example.userservice.auth.adapter.out.client;

import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.exception.AuthUserPortErrorCode;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.PortException;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.exception.UserErrorCode;
import com.example.userservice.user.fixture.UserResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthUserAdapterTest {

    @InjectMocks
    private AuthUserAdapter authUserAdapter;

    @Mock
    private UserQueryService userQueryService;

    @Test
    @DisplayName("유저 인증 정보를 반환한다.")
    void authenticate(){
        //given
        UserIdentityResult userIdentityResult = UserResultFixture.anUserIdentityResult().build();
        given(userQueryService.authenticate(anyString(), anyString())).willReturn(userIdentityResult);
        //when
        AuthUserResult result = authUserAdapter.authenticate("la9814@naver.com", "password1234*");
        //then
        assertThat(result.id()).isEqualTo(userIdentityResult.userId());
        assertThat(result.email()).isEqualTo(userIdentityResult.email());
        assertThat(result.name()).isEqualTo(userIdentityResult.name());
        assertThat(result.role()).isEqualTo(userIdentityResult.role());
    }

    @ParameterizedTest
    @EnumSource(value = UserErrorCode.class, names = {"USER_NOT_FOUND", "PASSWORD_NOT_MATCH"})
    @DisplayName("사용자 인증 실패 예외를 인증 정보 오류로 변환한다.")
    void authenticate_whenUserAuthenticationFailed_thenThrownInvalidCredentialsException(UserErrorCode errorCode){
        //given
        given(userQueryService.authenticate(anyString(), anyString()))
                .willThrow(new BusinessException(errorCode));
        //when
        //then
        assertThatThrownBy(() -> authUserAdapter.authenticate("email", "encryptedPassword"))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(AuthUserPortErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("예상하지 못한 예외가 발생하면 서버 오류로 변환한다.")
    void authenticate_whenUnexpectedExceptionThrown_thenThrowPortExceptionWithServerError(){
        //given
        given(userQueryService.authenticate(anyString(), anyString()))
                .willThrow(RuntimeException.class);
        //when
        //then
        assertThatThrownBy(() -> authUserAdapter.authenticate("email", "encryptedPassword"))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(AuthUserPortErrorCode.USER_SYSTEM_ERROR);
    }

    @Test
    @DisplayName("사용자 인증 정보를 조회한다.")
    void getUserById(){
        //given
        UserIdentityResult userIdentityResult = UserResultFixture.anUserIdentityResult().build();
        given(userQueryService.getUserIdentity(anyLong())).willReturn(userIdentityResult);
        //when
        AuthUserResult result = authUserAdapter.getUserById(1L);
        //then
        assertThat(result.id()).isEqualTo(userIdentityResult.userId());
        assertThat(result.email()).isEqualTo(userIdentityResult.email());
        assertThat(result.name()).isEqualTo(userIdentityResult.name());
        assertThat(result.role()).isEqualTo(userIdentityResult.role());
    }

    @ParameterizedTest
    @EnumSource(value = UserErrorCode.class, names = {"USER_NOT_FOUND", "PASSWORD_NOT_MATCH"})
    @DisplayName("사용자 인증 실패 예외를 인증 정보 오류로 변환한다.")
    void getUserId_whenUserAuthenticationFailed_thenThrownInvalidCredentialsException(UserErrorCode code){
        given(userQueryService.getUserIdentity(anyLong()))
                .willThrow(new BusinessException(code));
        //when
        //then
        assertThatThrownBy(() -> authUserAdapter.getUserById(1L))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(AuthUserPortErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("예상하지 못한 예외가 발생하면 서버 오류로 변환한다.")
    void getUserId_whenUnexpectedExceptionThrown_thenThrowPortExceptionWithServerError(){
        //given
        given(userQueryService.getUserIdentity(anyLong()))
                .willThrow(RuntimeException.class);
        //when
        //then
        assertThatThrownBy(() -> authUserAdapter.getUserById(1L))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(AuthUserPortErrorCode.USER_SYSTEM_ERROR);
    }
}