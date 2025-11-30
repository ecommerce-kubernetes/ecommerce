package com.example.order_service.controller.security;

import com.example.order_service.common.security.UserRole;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long userId() default 1L;
    UserRole userRole() default UserRole.ROLE_USER;
}
