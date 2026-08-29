package com.example.userservice.common.security;

import com.example.userservice.common.security.config.SecurityConfig;
import com.example.userservice.common.security.filter.CustomAccessDeniedHandler;
import com.example.userservice.common.security.filter.CustomAuthenticationEntryPoint;
import com.example.userservice.common.security.filter.HeaderPreAuthenticationFilter;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({
        SecurityConfig.class,
        HeaderPreAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
@WebMvcTest(controllers = SecurityTestController.class)
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidHeader")
    @DisplayName("헤더값이 유효하지 않으면 인증에 실패한다.")
    void validHeader(String description, HttpHeaders headers) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/security"));
    }

    @Test
    @DisplayName("X-User-Id 숫자 타입이 아닌경우 401 에러 응답을 반환한다")
    void validHeader_InvalidUserIdHeader() throws Exception {
        //given
        HttpHeaders invalidUserIdHeader = new HttpHeaders(MultiValueMap
                .fromSingleValue(Map.of("X-User-Id", "invalid", "X-User-Role", "ROLE_USER")));
        //when
        //then
        mockMvc.perform(get("/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(invalidUserIdHeader))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증 헤더 형식이 올바르지 않습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/security"));
    }

    @Test
    @DisplayName("X-User-Role 이 잘못된 문자열이면 401 에러 응답을 반환한다")
    void validHeader_InvalidUserRoleHeader() throws Exception {
        //given
        HttpHeaders invalidUserRoleHeader = new HttpHeaders(MultiValueMap
                .fromSingleValue(Map.of("X-User-Id", "1", "X-User-Role", "ROLE_INVALID")));
        //when
        //then
        mockMvc.perform(get("/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(invalidUserRoleHeader))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증 헤더 형식이 올바르지 않습니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/security"));
    }

    @Test
    @DisplayName("권한이 부족하면 권한 부족 에러 응답을 반환한다")
    void lack_of_permission() throws Exception {
        //given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_USER");
        //when
        //then
        mockMvc.perform(get("/security/permission")
                        .headers(headers)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/security/permission"));
    }

    @ParameterizedTest
    @DisplayName("인증 없이 접근 가능한 엔드포인트는 401 응답을 반환하지 않는다")
    @CsvSource({
            "POST, /users",
            "GET,  /users/email-availability?email=test@test.com",
            "POST, /auth/login",
            "POST, /auth/refresh"
    })
    void publicEndpoints(String method, String url) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.valueOf(method), url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status)
                            .isNotEqualTo(HttpStatus.UNAUTHORIZED.value())
                            .isNotEqualTo(HttpStatus.FORBIDDEN.value());
                });
    }

    @ParameterizedTest
    @DisplayName("인증이 필요한 엔드포인트에 미인증 접근 시 401 응답을 반환한다")
    @CsvSource({
            "POST,   /users/shipping-addresses",
            "DELETE, /users/shipping-addresses/1",
            "POST,   /auth/logout"
    })
    void protectedEndpoints(String method, String url) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"));
    }

    static Stream<Arguments> provideInvalidHeader() {
        HttpHeaders noUserIdHeader = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("X-User-Role", "ROLE_USER")));
        HttpHeaders noUserRoleHeader = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("X-User-Id", "1")));
        HttpHeaders noHeader = new HttpHeaders();
        return Stream.of(
                Arguments.of(
                        "유저 아이디가 누락되면 인증에 실패한다",
                        noUserIdHeader
                ),
                Arguments.of(
                        "유저 권한이 누락되면 인증에 실패한다",
                        noUserRoleHeader
                ),
                Arguments.of(
                        "헤더가 누락되면 인증에 실패한다",
                        noHeader
                )
        );
    }
}
