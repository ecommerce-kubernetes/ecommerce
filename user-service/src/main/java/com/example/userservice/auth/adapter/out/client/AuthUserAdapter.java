package com.example.userservice.auth.adapter.out.client;

import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.exception.AuthUserPortErrorCode;
import com.example.userservice.common.exception.PortException;
import com.example.userservice.user.application.service.UserQueryService;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserAdapter implements AuthUserPort {

    private final UserQueryService userQueryService;

    @Override
    public AuthUserResult authenticate(String email, String password) {
        UserIdentityResult userIdentityResult = executeAuthenticate(email, password);
        return mapToAuthUserResult(userIdentityResult);
    }

    private UserIdentityResult executeAuthenticate(String email, String password) {
        try {
            return userQueryService.authenticate(email, password);
        } catch (Exception e) {
            throw new PortException(AuthUserPortErrorCode.USER_SERVER_ERROR);
        }
    }

    private AuthUserResult mapToAuthUserResult(UserIdentityResult result) {
        return AuthUserResult.builder()
                .id(result.userId())
                .email(result.email())
                .name(result.name())
                .role(result.role())
                .build();
    }

    @Override
    public AuthUserResult getUserById(Long id) {
        return null;
    }
}
