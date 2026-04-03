package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

public class CategoryDescriptor {
    public static final FieldDescriptor ID = fieldWithPath("id").description("카테고리 ID");
    public static final FieldDescriptor NAME = fieldWithPath("name").description("카테고리 이름");
    public static final FieldDescriptor PARENT_ID = fieldWithPath("parentId").description("부모 카테고리 ID").type(JsonFieldType.NUMBER).optional();
    public static final FieldDescriptor DEPTH = fieldWithPath("depth").description("카테고리 깊이");
    public static final FieldDescriptor IMAGE_PATH = fieldWithPath("imagePath").description("카테고리 이미지 경로");

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("카테고리 이름"),
                PARENT_ID,
                fieldWithPath("imagePath").description("카테고리 아이콘 경로")
        };
    }

    public static FieldDescriptor[] getCategoryResponse() {
        return new FieldDescriptor[] {
                ID, NAME, PARENT_ID, DEPTH, IMAGE_PATH
        };
    }

    public static FieldDescriptor[] getTreeResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("[].id").description("카테고리 ID"),
                fieldWithPath("[].name").description("카테고리 이름"),
                fieldWithPath("[].parentId").description("부모 카테고리 ID").optional(),
                fieldWithPath("[].depth").description("카테고리 깊이"),
                fieldWithPath("[].imagePath").description("카테고리 이미지 URL"),
                subsectionWithPath("[].children").description("하위 카테고리 목록 (상위 구조와 동일)")
        };
    }

    public static FieldDescriptor[] getNavigationResponse() {
        return new FieldDescriptor[] {
                subsectionWithPath("current").description("요청 카테고리"),
                subsectionWithPath("path").description("직계 카테고리"),
                subsectionWithPath("siblings").description("형제 카테고리"),
                subsectionWithPath("children").description("자식 카테고리")
        };
    }

    public static FieldDescriptor[] getUpdateRequest() {
        return new FieldDescriptor[] {
                NAME, IMAGE_PATH
        };
    }

    public static FieldDescriptor[] getMoveCategoryRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("parentId").description("이동할 부모 카테고리 ID").optional()
        };
    }
}
