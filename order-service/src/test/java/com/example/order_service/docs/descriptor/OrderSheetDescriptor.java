package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderSheetDescriptor {

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].productVariantId").description("상품 변형 아이디"),
                fieldWithPath("items[].quantity").description("주문 수량")
        };
    }

    public static FieldDescriptor[] getCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("sheetId").description("주문서 id"),
                fieldWithPath("expiresAt").description("주문서 만료 시간")
        };
    }
}
