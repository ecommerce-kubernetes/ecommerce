package com.example.userservice.auth.adapter.out.client;

import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import org.springframework.stereotype.Component;

@Component
public class AuthUserAdapter implements AuthUserPort {
    @Override
    public AuthUserResult getUserByEmail(String email) {
        return null;
    }

    @Override
    public AuthUserResult getUserById(Long id) {
        return null;
    }
}
