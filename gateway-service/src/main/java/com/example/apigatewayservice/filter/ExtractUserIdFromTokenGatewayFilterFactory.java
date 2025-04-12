package com.example.apigatewayservice.filter;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class ExtractUserIdFromTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<ExtractUserIdFromTokenGatewayFilterFactory.Config> {

    Environment env;

    public ExtractUserIdFromTokenGatewayFilterFactory(Environment env) {
        super(Config.class);
        this.env = env;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Custom Pre Filter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    byte[] secretKeyBytes = Base64.getEncoder().encode(env.getProperty("token.secret").getBytes());
                    SecretKey signingKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());
                    // JWT 파싱
                    JwtParser jwtParser = Jwts.parser().verifyWith(signingKey).build();
                    String userId = jwtParser.parseSignedClaims(token).getPayload().getSubject(); // 사용자 ID 추출
                    log.info("Extracted userId from token: {}", userId);

                    // userId를 헤더에 추가
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("user-id", userId)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception e) {
                    log.error("JWT parsing failed: {}", e.getMessage());
                    // 예: 인증 실패 시 401 반환
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            // 인증 정보가 없으면 그대로 진행
            return chain.filter(exchange);
        };

    }

    public static class Config {
        // Put the configuration properties
    }
}
