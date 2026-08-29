package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.*;
import com.example.userservice.user.domain.model.User;
import com.example.userservice.user.domain.repository.UserRepository;
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

    public UserCreateResult createUser(UserCreateCommand command) {
        return null;
    }

    @Transactional(readOnly = true)
    public UserOrderResponse getUserInfoForOrder(Long userId) {
        User user = findByIdOrThrow(userId);
        return UserOrderResponse.from(user);
    }

    @Transactional(readOnly = true)
    public EmailAvailableResult checkAvailableEmail(String email) {
        return null;
    }

    public AddShippingAddressResult addShippingAddress(AddShippingAddressCommand command) {
        return null;
    }

    public void deleteShippingAddress(Long userId, Long shippingAddressId) {
    }

    @Transactional(readOnly = true)
    public UserProfileResult getUserProfile(Long userId) {
        return null;
    }

    @Transactional(readOnly = true)
    public UserPointsResult getUserPoints(Long userId) {
        return null;
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
