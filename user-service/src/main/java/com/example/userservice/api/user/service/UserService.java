package com.example.userservice.api.user.service;

import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import com.example.userservice.api.user.service.dto.result.UserOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserCreateResponse createUser(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.getEmail())){
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
        String encryptPwd = passwordEncoder.encode(command.getPassword());
        User user = userRepository.save(User.createUser(command, encryptPwd));
        return UserCreateResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserOrderResponse getUserInfoForOrder(Long userId) {
        User user = findByIdOrThrow(userId);
        return UserOrderResponse.from(user);
    }

    public void deductPoints(Long userId, Long point) {
        User user = findByIdOrThrow(userId);
        user.deductPoint(point);
    }

    public void refundPoints(Long userId, Long point) {
        User user = findByIdOrThrow(userId);
        user.refundPoint(point);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
