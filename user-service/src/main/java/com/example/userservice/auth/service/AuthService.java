package com.example.userservice.auth.service;

import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.domain.RefreshToken;
import com.example.userservice.auth.domain.repository.RefreshTokenRepository;
import com.example.userservice.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
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
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenData login(String email, String password) {
        AuthUserResult user = authUserPort.getUserByEmail(email);
        validatePassword(password, user.encryptedPwd());

        TokenData tokenData = jwtProvider.generateTokenData(user);

        return tokenData;
    }

    public TokenData refresh(String refreshToken) {
        Claims validClaims = jwtProvider.getValidClaims(refreshToken);
        Long userId = Long.parseLong(validClaims.getSubject());
        RefreshToken savedToken = tokenRepository.findById(userId);

        if (savedToken == null || !savedToken.getToken().equals(refreshToken)){
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        AuthUserResult user = authUserPort.getUserById(userId);
        TokenData tokenData = jwtProvider.generateTokenData(user);
        return tokenData;
    }

    public void logout(Long userId) {
        tokenRepository.deleteById(userId);
    }


    private void validatePassword(String password, String encryptPassword) {
        if (!passwordEncoder.matches(password, encryptPassword)) {
            throw new BusinessException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
    }
}
