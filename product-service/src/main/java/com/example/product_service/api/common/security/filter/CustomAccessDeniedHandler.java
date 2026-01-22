package com.example.product_service.api.common.security.filter;

import com.example.product_service.api.common.error.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LocalDateTime now = LocalDateTime.now();
        writeErrorResponse(response, "요청 권한이 부족합니다", now, request.getRequestURI());
    }

    private void writeErrorResponse(HttpServletResponse response, String message, LocalDateTime requestAt, String requestUrl) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("FORBIDDEN")
                .message(message)
                .timestamp(requestAt.toString())
                .path(requestUrl)
                .build();
        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}
