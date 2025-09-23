package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.TokenService;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLoginUser;
import com.example.userservice.vo.TokenPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private TokenService tokenService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, TokenService tokenService) {
        super(authenticationManager);
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {

            RequestLoginUser credentials = new ObjectMapper().readValue(request.getInputStream(), RequestLoginUser.class);
            log.info("credentials: {}", credentials);
            //받은 이메일, 패스워드 사용 >> UsernamePasswordAuthenticationToken 얻어 인증처리해주는 매니저에 넘김
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword(),
                            new ArrayList<>()
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String username = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserByEmail(username);

        String role = authResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        TokenPair tokenPair = tokenService.generateTokenPair(userDetails.getId(), role);
        ResponseCookie cookie = tokenService.setCookieRefreshToken(tokenPair.getRefreshToken());

        response.setHeader("Set-Cookie", cookie.toString());
        response.addHeader("token", tokenPair.getAccessToken());
    }
}
