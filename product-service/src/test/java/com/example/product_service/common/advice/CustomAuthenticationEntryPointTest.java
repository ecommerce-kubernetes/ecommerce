package com.example.product_service.common.advice;

import com.example.product_service.common.advice.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CustomAuthenticationEntryPointTest {
    CustomAuthenticationEntryPoint entryPoint;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp(){
        entryPoint = new CustomAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        request.setRequestURI("/path");
        response = new MockHttpServletResponse();
    }

    @Test
    void commence_writeExpected401Json() throws IOException {
        entryPoint.commence(request, response, new BadCredentialsException("bad credential"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse err = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(response.getContentAsString(), ErrorResponse.class);

        assertThat(err.getError()).isEqualTo("UnAuthorized");
        assertThat(err.getPath()).isEqualTo("/path");
        assertThat(err.getMessage()).isEqualTo("Invalid Header");
        assertThat(err.getTimestamp()).isNotNull();
    }
}