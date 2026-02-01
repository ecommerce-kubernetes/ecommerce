package com.example.userservice.docs.auth;

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

import static com.example.userservice.api.support.fixture.AuthRequestFixture.anLoginRequest;
import static com.example.userservice.api.support.fixture.AuthResponseFixture.anTokenData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerDocsTest extends RestDocsSupport {

    private AuthService authService = Mockito.mock(AuthService.class);

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
        //when
        //then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("auth-login",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("email").description("유저 이메일").optional(),
                                        fieldWithPath("password").description("유저 비밀번호").optional()
                                ),
                                responseCookies(
                                        cookieWithName("refreshToken").description("리프레시 토큰")
                                ),
                                responseFields(
                                        fieldWithPath("accessToken").description("액세스 토큰")
                                )
                        )
                );
    }
}
