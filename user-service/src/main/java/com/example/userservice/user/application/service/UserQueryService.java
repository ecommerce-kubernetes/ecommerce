package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import com.example.userservice.user.domain.ShippingAddress;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.util.PasswordManager;
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

        user.authenticate(password, passwordManager);

        return UserIdentityResult.builder()
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public UserProfileResult getUserProfile(Long userId) {
        User user = findByIdOrThrow(userId);

        return UserProfileResult.builder()
                .userId(user.getId())
                .userName(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .availablePoints(user.getPoint().longValue())
                .defaultShippingAddress(toShippingAddressResult(user.getDefaultShippingAddress()))
                .build();
    }

    public EmailAvailableResult checkAvailableEmail(String email) {
        return EmailAvailableResult.of(!userRepository.existsByEmail(email));
    }

    public UserBalanceResult getUserPoints(Long userId) {
        User user = findByIdOrThrow(userId);

        return UserBalanceResult.builder()
                .userId(user.getId())
                .availablePoints(user.getPoint().longValue())
                .build();
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private UserProfileResult.ShippingAddressResult toShippingAddressResult(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }

        return UserProfileResult.ShippingAddressResult.builder()
                .receiverName(shippingAddress.getReceiverName())
                .receiverPhone(shippingAddress.getReceiverPhone())
                .zipCode(shippingAddress.getZipCode())
                .address(shippingAddress.getAddress())
                .addressDetail(shippingAddress.getAddressDetail())
                .build();
    }
}
