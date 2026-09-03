package com.example.userservice.docs.auth;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class AuthDescriptor {

    public static FieldDescriptor[] loginRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("email")
                        .type(JsonFieldType.STRING)
                        .description("회원 이메일")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("password")
                        .type(JsonFieldType.STRING)
                        .description("회원 비밀번호")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] authResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("accessToken").description("액세스 토큰")
        };
    }
}
