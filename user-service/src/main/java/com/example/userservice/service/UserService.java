package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    UserEntity createUser(UserDto userDto);

    List<UserDto> getUserByAll();

    UserEntity getUserById(Long userId);

    UserDto getUserByEmail(String email);
}
