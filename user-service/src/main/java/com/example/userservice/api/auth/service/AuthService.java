package com.example.userservice.api.auth.service;

import com.example.userservice.api.auth.service.dto.TokenData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public TokenData login(String email, String password) {
        return null;
    }
}
