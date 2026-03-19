package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.domain.model.RefreshToken;
import com.example.userservice.api.auth.domain.repository.RefreshTokenRepository;
import com.example.userservice.api.auth.service.dto.JwtClaims;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenData login(String email, String password) {
        User user = findByEmailOrThrow(email);
        validatePassword(password, user.getEncryptedPwd());
        JwtClaims jwtClaims = JwtClaims.of(user);
        TokenData tokenData = jwtProvider.generateTokenData(jwtClaims);
        RefreshToken refreshToken = RefreshToken.create(user.getId(), tokenData.getRefreshToken());
        tokenRepository.save(refreshToken, jwtProvider.getRefreshTokenExpiration());
        return tokenData;
    }

    public TokenData refresh(String refreshToken) {
        Claims validClaims = jwtProvider.getValidClaims(refreshToken);
        Long userId = Long.parseLong(validClaims.getSubject());
        RefreshToken savedToken = tokenRepository.findById(userId);

        if (savedToken == null || !savedToken.getToken().equals(refreshToken)){
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = findByIdOrThrow(userId);
        JwtClaims jwtClaims = JwtClaims.of(user);
        TokenData tokenData = jwtProvider.generateTokenData(jwtClaims);
        RefreshToken newRefreshToken = RefreshToken.create(user.getId(), tokenData.getRefreshToken());
        tokenRepository.save(newRefreshToken, jwtProvider.getRefreshTokenExpiration());
        return tokenData;
    }

    private User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validatePassword(String password, String encryptPassword) {
        if (!passwordEncoder.matches(password, encryptPassword)) {
            throw new BusinessException(AuthErrorCode.PASSWORD_NOT_MATCH);
        }
    }
}
