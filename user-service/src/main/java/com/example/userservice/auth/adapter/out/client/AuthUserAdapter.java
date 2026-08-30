package com.example.userservice.auth.adapter.out.client;

import com.example.userservice.auth.application.port.AuthUserPort;
import com.example.userservice.auth.application.port.dto.AuthUserResult;
import com.example.userservice.auth.exception.AuthUserPortErrorCode;
import com.example.userservice.common.exception.BusinessException;
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
        } catch (BusinessException e) {
            AuthUserPortErrorCode code = translateErrorCode(e.getErrorCode().name());
            throw new PortException(code);
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
        UserIdentityResult userIdentityResult = executeGetIdentity(id);
        return mapToAuthUserResult(userIdentityResult);
    }

    private UserIdentityResult executeGetIdentity(Long userId) {
        try {
            return userQueryService.getUserIdentity(userId);
        } catch (BusinessException e) {
            AuthUserPortErrorCode code = translateErrorCode(e.getErrorCode().name());
            throw new PortException(code);
        } catch (Exception e) {
            throw new PortException(AuthUserPortErrorCode.USER_SERVER_ERROR);
        }
    }

    private AuthUserPortErrorCode translateErrorCode(String code) {
        return switch (code) {
            case "USER_NOT_FOUND", "PASSWORD_NOT_MATCH" -> AuthUserPortErrorCode.INVALID_CREDENTIALS;
            default -> AuthUserPortErrorCode.USER_SERVER_ERROR;
        };
    }
}
