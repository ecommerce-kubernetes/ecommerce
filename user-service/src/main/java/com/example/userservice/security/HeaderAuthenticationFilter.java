package com.example.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        // 헤더가 없으면 인증 없이 통과
        if (userIdHeader == null || roleHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            String role = roleHeader;

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            // userId를 principal로 사용
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("헤더 기반 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
