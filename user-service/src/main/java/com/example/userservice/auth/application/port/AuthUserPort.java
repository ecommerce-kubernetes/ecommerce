package com.example.userservice.auth.application.port;

import com.example.userservice.auth.application.port.dto.AuthUserResult;

public interface AuthUserPort {

    AuthUserResult authenticate(String email, String password);

    AuthUserResult getUserById(Long id);
}
