package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.CreateUserCommand;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import org.springframework.stereotype.Component;

@Component
public class UserContextFactory {

    public CreateUserContext createUserContext(Long id, CreateUserCommand command, String encryptedPassword) {
        return CreateUserContext.builder()
                .id(id)
                .email(command.email())
                .encryptedPassword(encryptedPassword)
                .name(command.name())
                .birthDate(command.birthDate())
                .gender(command.gender())
                .phoneNumber(command.phoneNumber())
                .build();
    }

    public CreateShippingAddressContext createShippingAddressContext(Long id, AddShippingAddressCommand command) {
        return CreateShippingAddressContext.builder()
                .id(id)
                .receiverName(command.receiverName())
                .receiverPhone(command.receiverPhone())
                .zipCode(command.zipCode())
                .address(command.address())
                .addressDetail(command.addressDetail())
                .isDefault(command.isDefault())
                .build();
    }
}
