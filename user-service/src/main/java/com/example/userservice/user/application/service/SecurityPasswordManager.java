package com.example.userservice.user.application.service;

import com.example.userservice.user.domain.util.PasswordManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityPasswordManager implements PasswordManager {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encrypt(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String password, String encryptPassword) {
        return passwordEncoder.matches(password, encryptPassword);
    }
}
