package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurity {

    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Environment env;
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    private static final String[] WHITE_LIST = {
            "/login/**", "/health_check/**", "/actuator/**", "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**"
    };


    public WebSecurity(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, Environment env, JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.env = env;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                                .anyRequest().authenticated()
                )
                .authenticationManager(authenticationManager)
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(getAuthenticationFilter(authenticationManager))
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception{
        AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, userService, env);
        filter.setFilterProcessesUrl("/login"); // ✅ 이 줄이 핵심!
        return filter;
    }
}
