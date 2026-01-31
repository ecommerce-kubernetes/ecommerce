package com.example.userservice.api.user.controller;

import com.example.userservice.api.support.ControllerTestSupport;
import com.example.userservice.api.support.security.config.TestSecurityConfig;
import com.example.userservice.api.user.controller.dto.UserCreateRequest;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static com.example.userservice.api.support.fixture.UserRequestFixture.anUserCreateRequest;
import static com.example.userservice.api.support.fixture.UserResponseFixture.anUserCreateResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class UserControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("회원을 생성한다")
    void createUser() throws Exception {
        //given
        UserCreateRequest request = anUserCreateRequest().build();
        UserCreateResponse response = anUserCreateResponse().build();
        given(userService.createUser(any(UserCreateCommand.class)))
                        .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("회원 생성 요청 검증")
    @MethodSource("provideInvalidCreateRequest")
    void createUser_validation(String description, UserCreateRequest request, String errorMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/users"));
    }


    static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("이메일이 없음", anUserCreateRequest().email(null).build(), "이메일은 필수 입력값입니다"),
                Arguments.of("잘못된 이메일 형식", anUserCreateRequest().email("invalidEmail").build(), "올바른 이메일 형식을 입력해주세요"),

                Arguments.of("비밀번호가 없음", anUserCreateRequest().password(null).build(), "비밀번호는 필수 입력값입니다"),
                Arguments.of("잘못된 비밀번호 형식", anUserCreateRequest().password("asdf").build(), "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다"),

                Arguments.of("이름이 없음", anUserCreateRequest().name(null).build(), "이름은 필수 입력값입니다"),
                Arguments.of("잘못된 이름 형식", anUserCreateRequest().name("이").build(), "이름은 2글자~12글자 사이여야 합니다"),

                Arguments.of("생년월일이 없음", anUserCreateRequest().birthDate(null).build(), "생년월일은 필수 입력값입니다"),

                Arguments.of("성별이 없음", anUserCreateRequest().gender(null).build(), "성별은 필수 입력값입니다"),
                Arguments.of("잘못된 성별 형식", anUserCreateRequest().gender("남자").build(), "성별은 MALE 또는 FEMALE 이어야 합니다"),

                Arguments.of("전화번호가 없음", anUserCreateRequest().phoneNumber(null).build(), "전화번호는 필수 입력값 입니다"),
                Arguments.of("잘못된 전화번호 형식", anUserCreateRequest().phoneNumber("01012345678").build(), "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        );
    }
}
