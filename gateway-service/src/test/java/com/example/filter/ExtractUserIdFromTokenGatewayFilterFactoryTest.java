package com.example.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExtractUserIdFromTokenGatewayFilterFactoryTest {

    private ExtractUserIdFromTokenGatewayFilterFactory factory;
    private Environment env;
    private String token;
    private String secret;

    @BeforeEach
    void setUp() {
        env = mock(Environment.class);

        // 시크릿 키: 64바이트 이상 권장
        secret = "mytestsecretmytestsecretmytestsecretmytestsecret1234567890123456";
        when(env.getProperty("token.secret")).thenReturn(secret);

        // 필터에서처럼 Base64 인코딩 후 SecretKey 생성
        byte[] encodedKey = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        SecretKey key = new SecretKeySpec(encodedKey, SignatureAlgorithm.HS512.getJcaName());

        // JWT 생성
        token = Jwts.builder()
                .setSubject("test-user-123")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        factory = new ExtractUserIdFromTokenGatewayFilterFactory(env);
    }

    @Test
    void testTokenIsParsedAndHeaderIsAdded() {
        // 가짜 요청 생성
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain mockChain = mock(GatewayFilterChain.class);
        when(mockChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange mutatedExchange = invocation.getArgument(0);
            String userIdHeader = mutatedExchange.getRequest().getHeaders().getFirst("user-id");
            assertThat(userIdHeader).isEqualTo("test-user-123");
            return Mono.empty();
        });

        Mono<Void> result = factory.apply(new ExtractUserIdFromTokenGatewayFilterFactory.Config()).filter(exchange, mockChain);

        result.block(); // 테스트 실행
        verify(mockChain, times(1)).filter(any(ServerWebExchange.class));
    }
}