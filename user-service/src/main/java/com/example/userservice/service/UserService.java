package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserEntity createUser(UserDto userDto);

    Page<UserDto> getUserByAll(Pageable pageable);

    UserDto getUserById(Long userId);

    UserDto getUserByEmail(String email);
}
