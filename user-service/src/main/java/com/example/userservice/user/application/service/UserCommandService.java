package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
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
    private final UserContextFactory contextFactory;

    public Long createUser(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        CreateUserContext context = contextFactory.createUserContext(command);

        User user = User.create(context, passwordManager, idGenerator);
        userRepository.save(user);
        return user.getId();
    }

    public void addShippingAddress(AddShippingAddressCommand command) {
        User user = findByIdOrThrow(command.userId());

        CreateShippingAddressContext context = contextFactory.createShippingAddressContext(command);

        user.addShippingAddress(context, idGenerator);
        userRepository.save(user);
    }

    public void deleteShippingAddress(Long userId, Long shippingAddressId) {
        User user = findByIdOrThrow(userId);
        user.removeShippingAddress(shippingAddressId);
        userRepository.save(user);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
