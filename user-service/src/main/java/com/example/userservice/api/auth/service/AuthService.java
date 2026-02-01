package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.domain.repository.RefreshTokenRepository;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.api.common.exception.AuthErrorCode;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenData login(String email, String password) {
        User user = findByEmailOrThrow(email);
        validatePassword(password, user.getEncryptedPwd());
        TokenData tokenData = tokenGenerator.generateTokenData(user.getId(), user.getRole());
        tokenRepository.save(user.getId(), tokenData.getRefreshToken(), tokenGenerator.getRefreshTokenExpiration());
        return tokenData;
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
