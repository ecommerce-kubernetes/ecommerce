package com.example.userservice.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider){
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

    protected HttpHeaders createAuthHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-access-token");
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_USER");
        return headers;
    }

    protected static final HeaderDescriptor[] AUTH_HEADER = new HeaderDescriptor[]{
            headerWithName("Authorization").description("인증 토큰")
    };
}
