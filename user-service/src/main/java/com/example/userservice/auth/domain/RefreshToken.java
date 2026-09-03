package com.example.userservice.auth.domain;

import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Duration;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    private Long userId;
    private String token;
    private Duration ttl;

    @Builder(builderMethodName = "reconstitute")
    private RefreshToken(Long userId, String token, Duration ttl) {
        Assert.notNull(userId, "리프레시 토큰 생성시 유저 아이디는 필수입니다.");
        Assert.hasText(token, "리프레시 토큰 생성시 토큰 값은 필수입니다.");
        Assert.notNull(ttl, "리프레시 토큰 생성시 만료 기간은 필수입니다.");

        this.userId = userId;
        this.token = token;
        this.ttl = ttl;
    }

    public static RefreshToken create(CreateRefreshTokenContext context) {
        return RefreshToken.reconstitute()
                .userId(context.userId())
                .token(context.token())
                .ttl(context.ttl())
                .build();
    }

    public boolean isMatches(String target) {
        return token.equals(target);
    }
}
