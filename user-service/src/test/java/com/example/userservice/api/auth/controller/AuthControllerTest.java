package com.example.userservice.api.auth.controller;

import com.example.userservice.api.auth.controller.dto.LoginRequest;
import com.example.userservice.api.auth.service.dto.LoginResponse;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.support.ControllerTestSupport;
import com.example.userservice.api.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static com.example.userservice.api.support.fixture.AuthRequestFixture.anLoginRequest;
import static com.example.userservice.api.support.fixture.AuthResponseFixture.anLoginResponse;
import static com.example.userservice.api.support.fixture.AuthResponseFixture.anTokenData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class AuthControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        //given
        LoginRequest request = anLoginRequest().build();
        TokenData token = anTokenData().build();
        LoginResponse response = anLoginResponse().build();
        given(authService.login(anyString(), anyString())).willReturn(token);
        //when
        //then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(cookie().value("refreshToken", token.getRefreshToken()));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("로그인 요청 검증")
    @MethodSource("provideInvalidLoginRequest")
    void login_validation(String description, LoginRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    static Stream<Arguments> provideInvalidLoginRequest() {
        return Stream.of(
                Arguments.of("이메일이 없음", anLoginRequest().email(null).build(), "이메일은 필수 입력값 입니다"),
                Arguments.of("잘못된 이메일 형식", anLoginRequest().email("asdf").build(), "올바른 이메일 형식을 입력해주세요"),

                Arguments.of("비밀번호가 없음", anLoginRequest().password(null).build(), "비밀번호는 필수 입력값 입니다"),
                Arguments.of("잘못된 비밀번호 형식", anLoginRequest().password("asdf").build(), "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다")
        );
    }
}
