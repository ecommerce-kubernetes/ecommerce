package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import org.springframework.stereotype.Component;

@Component
public class UserContextFactory {

    public CreateUserContext createUserContext(UserCreateCommand command) {
        return CreateUserContext.builder()
                .email(command.getEmail())
                .password(command.getPassword())
                .name(command.getName())
                .birthDate(command.getBirthDate())
                .gender(command.getGender())
                .phoneNumber(command.getPhoneNumber())
                .build();
    }

    public CreateShippingAddressContext createShippingAddressContext(AddShippingAddressCommand command) {
        return CreateShippingAddressContext.builder()
                .receiverName(command.receiverName())
                .receiverPhone(command.receiverPhone())
                .zipCode(command.zipCode())
                .address(command.address())
                .addressDetail(command.addressDetail())
                .isDefault(command.isDefault())
                .build();
    }
}
