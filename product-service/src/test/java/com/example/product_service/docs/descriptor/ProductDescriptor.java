package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ProductDescriptor {

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("categoryId").description("카테고리 ID"),
                fieldWithPath("description").description("상품 설명").optional()
        };
    }

    public static FieldDescriptor[] getRegisterOptionRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("optionTypeIds").description("옵션 타입 Id 리스트")
        };
    }

    public static FieldDescriptor[] getCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id")
        };
    }

    public static FieldDescriptor[] getRegisterOptionResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("productId").description("상품 Id"),
                fieldWithPath("options[].optionTypeId").description("옵션 타입 Id"),
                fieldWithPath("options[].optionTypeName").description("옵션 타입 이름"),
                fieldWithPath("options[].priority").description("상품 옵션 순서")
        };
    }
}
