package com.example.product_service.common.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeaderAuthenticationFilterTest {
    private HeaderAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp(){
        filter = new HeaderAuthenticationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = spy(new MockFilterChain());

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Role, Id 헤더가 존재할때")
    void header_exists() throws ServletException, IOException {
        setRoleAndId(request);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Role 헤더만 존재시")
    void only_role_header_exists() throws ServletException, IOException {
        setRole(request);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Id 헤더만 존재시")
    void only_id_header_exists() throws ServletException, IOException {
        setId(request);

        filter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    private void setRoleAndId(MockHttpServletRequest request){
        setRole(request);
        setId(request);
    }

    private void setRole(MockHttpServletRequest request){
        request.addHeader("X-User-Role", "ADMIN_ROLE");
    }

    private void setId(MockHttpServletRequest request){
        request.addHeader("X-User-Id", 1L);
    }
}