package com.example.userservice.docs.auth;

import com.example.userservice.auth.adapter.in.web.AuthController;
import com.example.userservice.auth.adapter.in.web.dto.LoginRequest;
import com.example.userservice.auth.service.AuthService;
import com.example.userservice.auth.service.dto.TokenData;
import com.example.userservice.docs.RestDocsSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.example.userservice.auth.fixture.AuthRequestFixture.anLoginRequest;
import static com.example.userservice.auth.fixture.AuthResponseFixture.anTokenData;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerDocsTest extends RestDocsSupport {

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
        given(authService.login(anyString(), anyString()))
                        .willReturn(token);

        //when
        //then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("auth/login",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(AuthDescriptor.loginRequest()),
                                responseFields(AuthDescriptor.authResponse()),
                                responseCookies(
                                        cookieWithName("refreshToken").description("리프레시 토큰 (HttpOnly, Secure)")
                                )
                        )
                );
    }

    @Test
    @DisplayName("토큰 리프레시")
    void refresh() throws Exception {
        //given
        TokenData token = anTokenData().build();
        given(authService.refresh(anyString()))
                .willReturn(token);
        //when
        //then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("refreshToken", "token")))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("auth/refresh",
                                preprocessResponse(prettyPrint()),
                                requestCookies(
                                        cookieWithName("refreshToken").description("리프레시 토큰 (HttpOnly, Secure)")
                                ),
                                responseCookies(
                                        cookieWithName("refreshToken").description("리프레시 토큰 (HttpOnly, Secure)")
                                ),
                                responseFields(AuthDescriptor.authResponse())
                        )
                );
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader();
        willDoNothing().given(authService).logout(anyLong());
        //when
        //then
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andDo(
                        document("auth/logout",
                                preprocessRequest(
                                        prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                ),
                                requestHeaders(AUTH_HEADER)
                        )
                );
    }
}
