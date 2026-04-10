package com.example.apigatewayservice.filter;

import com.example.apigatewayservice.filter.properties.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String KEY_ROLE = "role";

    private final TokenProperties tokenProperties;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    public AuthorizationHeaderFilter(TokenProperties tokenProperties) {
        super(Config.class);
        this.tokenProperties = tokenProperties;
        this.secretKey = Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser().verifyWith(this.secretKey).build();
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return chain.filter(exchange);
            }
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
                return onError(exchange, "Authorization header must start with Bearer");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            log.error("token : {}", token);
            try {
                Claims claims = jwtParser.parseSignedClaims(token).getPayload();
                ServerHttpRequest mutatedRequest = mutateRequestWithClaims(request, claims);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                log.warn("JWT 검증 실패: {}", e.getMessage());
                return onError(exchange, "유효하지 않은 JWT 토큰입니다.");
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format("{\"error\": \"%s\"}", errMessage);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));

        log.error("인증 오류: {}", errMessage);
        return response.writeWith(Mono.just(buffer));
    }

    private ServerHttpRequest mutateRequestWithClaims(ServerHttpRequest request, Claims claims) {
        String userId = claims.getSubject();
        String role = claims.get(KEY_ROLE, String.class);

        // 검증 로직: userId 필수 체크
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("Token subject(userId) is missing");
        }

        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .header(HEADER_USER_ID, userId);

        if (StringUtils.hasText(role)) {
            requestBuilder.header(HEADER_USER_ROLE, role);
        }

        return requestBuilder.build();
    }
}
