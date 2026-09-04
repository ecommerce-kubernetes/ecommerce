package com.example.product_service.docs;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.product_service.common.security.model.UserPrincipal;
import com.example.product_service.common.security.model.UserRole;
import com.example.product_service.support.fixture.FixtureMonkeyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.navercorp.fixturemonkey.FixtureMonkey;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
        };
    }

    protected static final HeaderDescriptor[] AUTH_HEADER = new HeaderDescriptor[]{
            headerWithName("Authorization").description("인증 토큰")
    };

    protected HttpHeaders createAuthHeader(String role){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-access-token");
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", role);
        return headers;
    }
}
