package com.example.order_service.api.common.security.filter;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.model.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class HeaderPreAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-User-Id");
        String userRoleHeader = request.getHeader("X-User-Role");

        if (userIdHeader != null && userRoleHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                UserRole userRole = UserRole.valueOf(userRoleHeader);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
                UserPrincipal userPrincipal = UserPrincipal.of(userId, userRole);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                log.warn("인증 헤더 형식 오류");
                request.setAttribute("authError", "인증 헤더 형식이 올바르지 않습니다");
            }
        }

        filterChain.doFilter(request, response);
    }
}
