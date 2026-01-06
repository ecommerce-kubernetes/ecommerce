package com.example.product_service.api.common.security.filter;

import com.example.product_service.api.common.error.dto.response.ErrorResponse;
import com.example.product_service.api.common.security.model.UserPrincipal;
import com.example.product_service.api.common.security.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class HeaderPreAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-User-Id");
        String userRoleHeader = request.getHeader("X-User-Role");
        LocalDateTime requestAt = LocalDateTime.now();

        if(userIdHeader == null || userRoleHeader == null){
            return;
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            UserRole userRole = UserRole.valueOf(userRoleHeader);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userRole.name()));

            UserPrincipal userPrincipal = UserPrincipal.of(userId, userRole);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal,
                    null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (NumberFormatException e) {
            writeErrorResponse(response, "X-User-Id 헤더가 유효하지 않습니다", requestAt, request.getRequestURI());
            return;
        } catch (IllegalArgumentException e) {
            writeErrorResponse(response, "X-User-Role 헤더가 유효하지 않습니다", requestAt, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
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
