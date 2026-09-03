package com.example.userservice.user.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.application.port.PasswordManager;
import com.example.userservice.user.exception.UserErrorCode;
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
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encryptedPassword = passwordManager.encrypt(command.password());
        Long id = idGenerator.generate();

        CreateUserContext context = contextFactory.createUserContext(id, command, encryptedPassword);

        User user = User.create(context);
        userRepository.save(user);
        return user.getId();
    }

    public void addShippingAddress(AddShippingAddressCommand command) {
        User user = findByIdOrThrow(command.userId());

        Long id = idGenerator.generate();
        CreateShippingAddressContext context = contextFactory.createShippingAddressContext(id, command);

        user.addShippingAddress(context);
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
