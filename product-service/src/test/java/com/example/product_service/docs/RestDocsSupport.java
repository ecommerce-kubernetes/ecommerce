package com.example.product_service.docs;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.product_service.api.common.security.model.UserPrincipal;
import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.support.fixture.FixtureMonkeyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setCustomArgumentResolvers(getArgumentResolvers())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .apply(documentationConfiguration(provider))
                .build();
    }

    protected abstract String getTag();

    protected RestDocumentationResultHandler createDocument(
            String identifier,
            String summary,
            String description,
            FieldDescriptor[] requestFields,
            FieldDescriptor[] responseFields) {

        return document(identifier,
                preprocessRequest(prettyPrint(),
                        modifyHeaders().remove("X-User-Id").remove("X-User-Role").add("Authorization", "Bearer {ACCESS_TOKEN}")),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                        .tag(getTag())
                        .summary(summary)
                        .description(description)
                        .requestFields(requestFields)
                        .responseFields(responseFields)
                        .build()),
                requestFields(requestFields),
                responseFields(responseFields)
        );
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
            HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            if (userId == null) {
                return null;
            }

            return UserPrincipal.of(Long.parseLong(userId), UserRole.valueOf(userRole));
        }
    }
}
