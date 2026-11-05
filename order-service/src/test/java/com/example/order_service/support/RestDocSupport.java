package com.example.order_service.support;

import com.example.order_service.common.security.model.UserPrincipal;
import com.example.order_service.common.security.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocSupport {
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider){
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setCustomArgumentResolvers(getArgumentResolvers())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .apply(documentationConfiguration(provider))
                .build();
    }

    protected static final HeaderDescriptor[] AUTH_HEADER = new HeaderDescriptor[]{
            headerWithName("Authorization").description("인증 토큰")
    };

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

    protected HttpHeaders createAuthHeader(String role){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-access-token");
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", role);
        return headers;
    }
}
