package com.example.product_service.api.common.security.filter;

import com.example.product_service.api.common.error.ControllerAdvice;
import com.example.product_service.api.common.security.config.SecurityConfig;
import com.example.product_service.support.DummyController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(
        controllers = DummyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {ControllerAdvice.class}
        )
)
public class SecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("provideInvalidHeader")
    @DisplayName("X-User-Id 헤더와 X-User-Role 헤더중 하나라도 없는 경우 401 에러 응답을 반환한다")
    void validHeader_NoHeader(HttpHeaders headers) throws Exception {
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

    @ParameterizedTest
    @CsvSource(value = {"1,ROLE_USER", "1,ROLE_ADMIN"})
    @DisplayName("X-User-Id 헤더와 X-User-Role 헤더에 유효한 값이 있는 경우 인증 객체가 저장된다")
    void validHeader_authenticationUser(String userId, String userRole) throws Exception {
        //given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        headers.add("X-User-Role", userRole);
        //when
        //then
        mockMvc.perform(get("/security")
                        .headers(headers)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("userId=" + userId + ",userRole=" + userRole));
    }

    private static Stream<Arguments> provideInvalidHeader() {
        HttpHeaders noUserIdHeader = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("X-User-Role", "ROLE_USER")));
        HttpHeaders noUserRoleHeader = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("X-User-Id", "1")));
        HttpHeaders noHeader = new HttpHeaders();
        return Stream.of(
                Arguments.of(
                        noUserIdHeader
                ),
                Arguments.of(
                        noUserRoleHeader
                ),
                Arguments.of(
                        noHeader
                )
        );
    }
}
