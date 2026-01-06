package com.example.product_service.docs;

import com.example.product_service.api.common.security.model.UserPrincipal;
import com.example.product_service.api.common.security.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
    protected static final String USER_ID_HEADER_DESCRIPTION = "회원 Id(회원 식별자)";
    protected static final String USER_ROLE_HEADER_DESCRIPTION = "회원 role(회원 권한)";
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setCustomArgumentResolvers(getArgumentResolvers())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .apply(documentationConfiguration(provider))
                .build();
    }

    protected abstract Object initController();

    protected HandlerMethodArgumentResolver[] getArgumentResolvers() {
        return new HandlerMethodArgumentResolver[]{
                new PageableHandlerMethodArgumentResolver(),
                new MockUserPrincipalArgumentResolver()
        };
    }

    static class MockUserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return UserPrincipal.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return UserPrincipal.of(1L, UserRole.ROLE_USER);
        }
    }
}
