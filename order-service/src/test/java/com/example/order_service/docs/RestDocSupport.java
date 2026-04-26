package com.example.order_service.docs;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.order_service.api.common.security.model.UserPrincipal;
import com.example.order_service.api.common.security.model.UserRole;
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
            headerWithName("Authorization").description("JWT Access Token")
    };

    protected abstract String getTag();

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

    private RestDocumentationResultHandler createDocument(
            String identifier, String summary, String description,
            HeaderDescriptor[] requestHeaders,
            FieldDescriptor[] requestFields,
            FieldDescriptor[] responseFields,
            ParameterDescriptor... pathParameters) {
        List<Snippet> snippets = new ArrayList<>();
        snippets.add(resource(ResourceSnippetParameters.builder()
                .tag(getTag())
                .summary(summary)
                .description(description)
                .requestHeaders(requestHeaders)
                .pathParameters(pathParameters)
                .requestFields(requestFields)
                .responseFields(responseFields)
                .build()));
        if (requestHeaders.length > 0) snippets.add(requestHeaders(requestHeaders));
        if (pathParameters.length > 0) snippets.add(pathParameters(pathParameters));
        if (requestFields.length > 0) snippets.add(requestFields(requestFields));
        if (responseFields.length > 0) snippets.add(responseFields(responseFields));
        return document(
                identifier,
                preprocessRequest(prettyPrint(), modifyHeaders().remove("X-User-Id").remove("X-User-Role")),
                preprocessResponse(prettyPrint()),
                snippets.toArray(new Snippet[0])
        );
    }

    protected RestDocumentationResultHandler createSecuredDocument(
            String identifier, String summary, String description,
            FieldDescriptor[] requestFields,
            FieldDescriptor[] responseFields,
            ParameterDescriptor... pathParameters) {
        return createDocument(identifier, summary, description, AUTH_HEADER, requestFields, responseFields, pathParameters);
    }

    protected RestDocumentationResultHandler createSecuredDocument(
            String identifier, String summary, String description,
            ParameterDescriptor... pathParameters) {
        return createDocument(identifier, summary, description, AUTH_HEADER,new FieldDescriptor[0], new FieldDescriptor[0], pathParameters);
    }

    protected RestDocumentationResultHandler createSecuredDocument(
            String identifier, String summary, String description,
            FieldDescriptor[] responseFields,
            ParameterDescriptor... pathParameters) {
        return createDocument(identifier, summary, description, AUTH_HEADER, new FieldDescriptor[0], responseFields, pathParameters);
    }


    protected RestDocumentationResultHandler createPublicDocument(
            String identifier, String summary, String description,
            FieldDescriptor[] responseFields,
            ParameterDescriptor... pathParameters) {

        return createDocument(identifier, summary, description, new HeaderDescriptor[0], new FieldDescriptor[0], responseFields, pathParameters);
    }

    protected HttpHeaders createAuthHeader(String role){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-access-token");
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", role);
        return headers;
    }
}
