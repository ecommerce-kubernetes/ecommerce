package com.example.userservice.api.support.security.annotation;

import com.example.userservice.api.support.security.WithCustomMockUserSecurityContextFactory;
import com.example.userservice.api.user.domain.model.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long userId() default 1L;
    Role userRole() default Role.ROLE_ADMIN;
}
