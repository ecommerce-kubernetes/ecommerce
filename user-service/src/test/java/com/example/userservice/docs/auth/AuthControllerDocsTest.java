package com.example.userservice.docs.auth;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.userservice.api.auth.controller.AuthController;
import com.example.userservice.api.auth.controller.dto.LoginRequest;
import com.example.userservice.api.auth.service.AuthService;
import com.example.userservice.api.auth.service.dto.TokenData;
import com.example.userservice.docs.RestDocsSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.cookies.CookieDescriptor;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.userservice.api.support.fixture.AuthRequestFixture.anLoginRequest;
import static com.example.userservice.api.support.fixture.AuthResponseFixture.anTokenData;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
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
                                                .description("""
                                                        이메일과 패스워드로 로그인
                                                        "테스트 유저 -> email : user@naver.com, password : user1234*
                                                        "테스트 어드민 -> email : admin@naver.com, password : admin1234*
                                                        """)
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

    @Test
    @DisplayName("토큰 리프레시")
    void refresh() throws Exception {
        //given
        TokenData token = anTokenData().build();
        CookieDescriptor[] responseCookies = new CookieDescriptor[] {
                cookieWithName("refreshToken").description("리프레시 토큰 (HttpOnly, Secure)")
        };
        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("accessToken").description("액세스 토큰")
        };
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
                        document("02-auth-02-refresh",
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("토큰 리프레시")
                                                .description("refresh 토큰을 이용하여 accessToken 재발급")
                                                .build()
                                ),
                                responseCookies(responseCookies),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader();
        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };
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
                        document("02-auth-03-logout",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("로그아웃")
                                                .description("리프레시 토큰과 쿠키를 삭제합니다")
                                                .requestHeaders(requestHeaders)
                                                .build()
                                ),
                                requestHeaders(requestHeaders)
                        )
                );
    }

    private HttpHeaders createAuthHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_USER");
        return headers;
    }
}
