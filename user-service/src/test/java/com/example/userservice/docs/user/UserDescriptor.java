package com.example.userservice.docs.user;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class UserDescriptor {

    public static FieldDescriptor[] userCreateRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("email")
                        .type(JsonFieldType.STRING)
                        .description("이메일")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("password")
                        .type(JsonFieldType.STRING)
                        .description("비밀번호")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("birthDate")
                        .type(JsonFieldType.STRING)
                        .description("생년월일")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("gender")
                        .type(JsonFieldType.STRING)
                        .description("성별 (MALE 또는 FEMALE)")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("phoneNumber")
                        .type(JsonFieldType.STRING)
                        .description("전화번호")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] userCreateResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("userId").description("생성된 유저 id(식별자)")
        };
    }

    public static FieldDescriptor[] emailAvailableResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("available").description("사용 가능 여부")
        };
    }

    public static FieldDescriptor[] addShippingAddressRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("receiverName")
                        .type(JsonFieldType.STRING)
                        .description("수령인 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("receiverPhone")
                        .type(JsonFieldType.STRING)
                        .description("수령인 전화번호")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("zipCode")
                        .type(JsonFieldType.STRING)
                        .description("우편번호")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("address")
                        .type(JsonFieldType.STRING)
                        .description("주소")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("addressDetail")
                        .type(JsonFieldType.STRING)
                        .description("상세주소")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] addShippingAddressResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("userId").description("배송지를 추가한 유저 id(식별자)")
        };
    }

    public static FieldDescriptor[] userProfileResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("userId").description("유저 id(식별자)"),
                fieldWithPath("userName").description("유저 이름"),
                fieldWithPath("phoneNumber").description("전화번호"),
                fieldWithPath("availablePoints").description("사용 가능 포인트"),
                fieldWithPath("defaultShippingAddress").description("대표 배송지").optional(),
                fieldWithPath("defaultShippingAddress.receiverName").description("수령인 이름").optional(),
                fieldWithPath("defaultShippingAddress.receiverPhone").description("수령인 전화번호").optional(),
                fieldWithPath("defaultShippingAddress.zipCode").description("우편번호").optional(),
                fieldWithPath("defaultShippingAddress.address").description("주소").optional(),
                fieldWithPath("defaultShippingAddress.addressDetail").description("상세주소").optional()
        };
    }

    public static FieldDescriptor[] userPointsResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("userId").description("유저 id(식별자)"),
                fieldWithPath("availablePoints").description("사용 가능 포인트")
        };
    }
}
