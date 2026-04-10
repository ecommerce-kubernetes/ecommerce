package com.example.userservice.api.support.security;

import com.example.userservice.api.common.security.model.UserPrincipal;
import com.example.userservice.api.support.security.annotation.WithCustomMockUser;
import com.example.userservice.api.user.domain.model.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import java.util.List;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        long userId = annotation.userId();
        Role userRole = annotation.userRole();
        UserPrincipal userPrincipal = UserPrincipal.of(userId, userRole);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null,
                        List.of(new SimpleGrantedAuthority(userRole.name())));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        return context;

    }

}
