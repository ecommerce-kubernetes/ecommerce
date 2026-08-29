package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.command.AddShippingAddressCommand;
import com.example.userservice.user.application.service.dto.command.UserCreateCommand;
import com.example.userservice.user.application.service.dto.result.AddShippingAddressResult;
import com.example.userservice.user.application.service.dto.result.UserCreateResult;
import com.example.userservice.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    public UserCreateResult createUser(UserCreateCommand command) {
        return null;
    }

    public AddShippingAddressResult addShippingAddress(AddShippingAddressCommand command) {
        return null;
    }


    public void deleteShippingAddress(Long userId, Long shippingAddressId) {
    }
}
