package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordManager passwordManager;
    private final IdGenerator idGenerator;

    public UserCreateResult createUser(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        CreateUserContext context = CreateUserContext.builder()
                .email(command.getEmail())
                .password(command.getPassword())
                .name(command.getName())
                .birthDate(command.getBirthDate())
                .gender(command.getGender())
                .phoneNumber(command.getPhoneNumber())
                .build();

        User user = User.createUser(context, passwordManager, idGenerator);
        User savedUser = userRepository.save(user);
        return UserCreateResult.from(savedUser);
    }

    public void addShippingAddress(AddShippingAddressCommand command) {
        User user = findByIdOrThrow(command.userId());

        CreateShippingAddressContext context = CreateShippingAddressContext.builder()
                .receiverName(command.receiverName())
                .receiverPhone(command.receiverPhone())
                .zipCode(command.zipCode())
                .address(command.address())
                .addressDetail(command.addressDetail())
                .isDefault(false)
                .build();

        user.addShippingAddress(context, idGenerator);
        userRepository.save(user);
    }

    public void deleteShippingAddress(Long userId, Long shippingAddressId) {
        User user = findByIdOrThrow(userId);
        user.removeShippingAddress(shippingAddressId);
        userRepository.save(user);
    }

    public void deductPoints(Long userId, Long point) {
        User user = findByIdOrThrow(userId);
        user.deductPoint(Money.wons(point));
        userRepository.save(user);
    }

    public void refundPoints(Long userId, Long point) {
        User user = findByIdOrThrow(userId);
        user.refundPoint(Money.wons(point));
        userRepository.save(user);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
