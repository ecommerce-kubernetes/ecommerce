package com.example.userservice.auth.application.port;

import com.example.userservice.auth.domain.RefreshToken;

import java.util.Optional;

public interface TokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
