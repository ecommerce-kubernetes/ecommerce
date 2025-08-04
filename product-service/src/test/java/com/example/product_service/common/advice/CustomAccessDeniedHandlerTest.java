package com.example.product_service.common.advice;

import com.example.product_service.common.advice.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAccessDeniedHandlerTest {
    CustomAccessDeniedHandler handler;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    private static final String PATH = "/path";
    private static final String ERROR = "Forbidden";
    private static final String MESSAGE = "Access Denied";

    @BeforeEach
    void setUp(){
        handler = new CustomAccessDeniedHandler();
        request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", 1);
        request.addHeader("X-User-Role", "ROLE_USER");
        request.setRequestURI(PATH);
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("권한 부족 핸들러 응답 테스트")
    void handle_writeExpected403Json() throws IOException {
        AccessDeniedException lackOfPermission = new AccessDeniedException("Lack of Permission");
        handler.handle(request, response,lackOfPermission);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse err = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(response.getContentAsString(), ErrorResponse.class);

        assertThat(err.getError()).isEqualTo(ERROR);
        assertThat(err.getPath()).isEqualTo(PATH);
        assertThat(err.getMessage()).isEqualTo(MESSAGE);
        assertThat(err.getTimestamp()).isNotNull();
    }
}