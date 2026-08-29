package com.example.userservice.common.security.filter;

import com.example.userservice.common.error.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String message = (String) request.getAttribute("authError");

        if (message == null) {
            message = "인증이 필요한 접근입니다";
        }

        writeErrorResponse(response, message, request.getRequestURI());
    }

    private void writeErrorResponse(HttpServletResponse response, String message, String requestUrl) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.of("UNAUTHORIZED", message, requestUrl);

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}
