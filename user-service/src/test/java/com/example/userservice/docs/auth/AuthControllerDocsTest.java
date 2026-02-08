package com.example.userservice.docs.auth;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.userservice.api.auth.controller.AuthController;
import com.example.userservice.api.auth.controller.dto.LoginRequest;
import com.example.userservice.api.auth.service.AuthService;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.cookies.CookieDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.userservice.api.support.fixture.AuthRequestFixture.anLoginRequest;
import static com.example.userservice.api.support.fixture.AuthResponseFixture.anTokenData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerDocsTest extends RestDocsSupport {

    private AuthService authService = Mockito.mock(AuthService.class);

    private static final String TAG = "AUTH";

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        //given
        LoginRequest request = anLoginRequest().build();
        TokenData token = anTokenData().build();
        BDDMockito.given(authService.login(anyString(), anyString()))
                        .willReturn(token);

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("email").description("유저 이메일"),
                fieldWithPath("password").description("유저 비밀번호")
        };

        CookieDescriptor[] responseCookies = new CookieDescriptor[] {
                cookieWithName("refreshToken").description("리프레시 토큰 (HttpOnly, Secure)")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("accessToken").description("액세스 토큰")
        };

        //when
        //then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-auth-01-login",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("로그인")
                                                .description("이메일과 패스워드로 로그인")
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestFields(requestFields),
                                responseCookies(responseCookies),
                                responseFields(responseFields)
                        )
                );
    }
}
