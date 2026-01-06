package com.example.product_service.api.common.security.filter;

import com.example.product_service.api.common.error.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

public class HeaderAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = (String) request.getAttribute("authError");
        LocalDateTime now = LocalDateTime.now();
        if (message == null) {
            message = "인증이 필요한 접근입니다";
        }
        writeErrorResponse(response, message, now, request.getRequestURI());
    }

    private void writeErrorResponse(HttpServletResponse response, String message, LocalDateTime requestAt, String requestUrl) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("UNAUTHORIZED")
                .message(message)
                .timestamp(requestAt.toString())
                .path(requestUrl)
                .build();
        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}
