package com.example.product_service.docs;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CategoryDescriptor {
    public static final FieldDescriptor ID = fieldWithPath("id").description("카테고리 ID");
    public static final FieldDescriptor NAME = fieldWithPath("name").description("카테고리 이름");
    public static final FieldDescriptor PARENT_ID = fieldWithPath("parentId").description("부모 카테고리 ID").type(JsonFieldType.NUMBER).optional();
    public static final FieldDescriptor DEPTH = fieldWithPath("depth").description("카테고리 깊이");
    public static final FieldDescriptor IMAGE_PATH = fieldWithPath("imageUrl").description("카테고리 이미지 경로");

    public static FieldDescriptor[] getRequestFields() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("카테고리 이름"),
                PARENT_ID,
                fieldWithPath("imagePath").description("카테고리 아이콘 경로")
        };
    }

    public static FieldDescriptor[] getResponseFields() {
        return new FieldDescriptor[] {
                ID, NAME, PARENT_ID, DEPTH, IMAGE_PATH
        };
    }
}
