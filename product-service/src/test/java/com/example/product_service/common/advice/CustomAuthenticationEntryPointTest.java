package com.example.product_service.common.advice;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {
    CustomAuthenticationEntryPoint entryPoint;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @Mock
    MessageSourceUtil ms;

    private static final String PATH = "/path";
    private static final String ERROR = "UnAuthorized";
    private static final String MESSAGE = "Invalid Header";

    @BeforeEach
    void setUp(){
        entryPoint = new CustomAuthenticationEntryPoint(ms);
        when(ms.getMessage("unAuthorized")).thenReturn(ERROR);
        when(ms.getMessage("unAuthorized.message")).thenReturn(MESSAGE);
        request = new MockHttpServletRequest();
        request.setRequestURI(PATH);
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("인증 엔트리 포인트 응답 검증")
    void commence_writeExpected401Json() throws IOException {
        entryPoint.commence(request, response, new BadCredentialsException("bad credential"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
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