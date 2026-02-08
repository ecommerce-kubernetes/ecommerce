package com.example.product_service.support.security.annotation;

import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.support.security.WithCustomMockUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long userId() default 1L;
    UserRole userRole() default UserRole.ROLE_ADMIN;
}
