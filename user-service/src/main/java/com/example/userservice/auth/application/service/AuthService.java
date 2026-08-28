package com.example.userservice.auth.application.service;

import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.TokenRepository;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.application.service.dto.TokenResult;
import com.example.userservice.auth.domain.RefreshToken;
import com.example.userservice.auth.domain.context.CreateRefreshTokenContext;
import com.example.userservice.auth.application.service.dto.TokenData;
import com.example.userservice.auth.exception.AuthErrorCode;
import com.example.userservice.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtProvider jwtProvider;
    private final AuthUserPort authUserPort;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenResult login(String email, String password) {
        AuthUserResult user = authUserPort.getUserByEmail(email);
        validatePassword(password, user.encryptedPwd());

        TokenData tokenData = jwtProvider.generateTokenData(user);

        RefreshToken refreshToken = createRefreshToken(user, tokenData);
        tokenRepository.save(refreshToken);
        return TokenResult.of(tokenData.accessToken(), tokenData.refreshToken());
    }

    public TokenResult refresh(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);

        RefreshToken findToken = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!findToken.isMatches(refreshToken)) {
            tokenRepository.deleteByUserId(userId);
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_MATCHES);
        }

        AuthUserResult user = authUserPort.getUserById(userId);
        TokenData tokenData = jwtProvider.generateTokenData(user);

        RefreshToken newRefreshToken = createRefreshToken(user, tokenData);
        tokenRepository.save(newRefreshToken);
        return TokenResult.of(tokenData.accessToken(), tokenData.refreshToken());
    }

    public void logout(Long userId) {
        tokenRepository.deleteByUserId(userId);
    }

    private void validatePassword(String password, String encryptPassword) {
        if (!passwordEncoder.matches(password, encryptPassword)) {
            throw new BusinessException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private RefreshToken createRefreshToken(AuthUserResult userResult, TokenData tokenData) {
        CreateRefreshTokenContext context = CreateRefreshTokenContext.builder()
                .userId(userResult.id())
                .token(tokenData.refreshToken())
                .ttl(tokenData.refreshTokenTtl())
                .build();

        return RefreshToken.create(context);
    }
}
