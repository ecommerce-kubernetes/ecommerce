package com.example.order_service.api.support.security.annotation;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.support.security.WithCustomMockUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long userId() default 1L;
    UserRole userRole() default UserRole.ROLE_USER;
}
