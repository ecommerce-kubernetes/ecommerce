package com.example.userservice.auth.application.port;

import com.example.userservice.auth.application.port.dto.AuthUserResult;

public interface AuthUserPort {

    AuthUserResult getUserByEmail(String email);

    AuthUserResult getUserById(Long id);
}
