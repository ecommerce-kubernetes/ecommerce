package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.application.port.PasswordManager;
import com.example.userservice.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    public UserIdentityResult authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordManager.matches(password, user.getEncryptedPwd())) {
            throw new BusinessException(UserErrorCode.PASSWORD_NOT_MATCH);
        }

        return UserIdentityResult.from(user);
    }

    public UserIdentityResult getUserIdentity(Long userId) {
        User user = findByIdOrThrow(userId);

        return UserIdentityResult.from(user);
    }

    public UserProfileResult getUserProfile(Long userId) {
        User user = findByIdOrThrow(userId);

        return UserProfileResult.from(user);
    }

    public EmailAvailableResult checkAvailableEmail(String email) {
        return EmailAvailableResult.of(!userRepository.existsByEmail(email));
    }

    public UserBalanceResult getUserPoints(Long userId) {
        User user = findByIdOrThrow(userId);

        return UserBalanceResult.from(user);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
