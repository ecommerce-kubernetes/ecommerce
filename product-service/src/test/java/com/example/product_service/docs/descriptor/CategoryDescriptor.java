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
    public static final FieldDescriptor IMAGE_PATH = fieldWithPath("imagePath").description("카테고리 이미지 경로").optional().type(JsonFieldType.STRING);

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("카테고리 이름"),
                PARENT_ID,
                fieldWithPath("imagePath").description("카테고리 아이콘 경로")
        };
    }

    public static FieldDescriptor[] getIdResponse() {
        return new FieldDescriptor[] {
                ID
        };
    }

    public static FieldDescriptor[] getRootListResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("categories[].id").description("카테고리 ID"),
                fieldWithPath("categories[].name").description("카테고리 이름"),
                fieldWithPath("categories[].imagePath").description("카테고리 이미지 경로").optional().type(JsonFieldType.STRING),
                fieldWithPath("categories[].isLeaf").description("리프(최하위) 카테고리 여부")
        };
    }

    public static FieldDescriptor[] getChildrenListResponse() {
        return getRootListResponse();
    }

    public static FieldDescriptor[] getTreeResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("categories[].id").description("카테고리 ID"),
                fieldWithPath("categories[].name").description("카테고리 이름"),
                fieldWithPath("categories[].depth").description("카테고리 깊이"),
                fieldWithPath("categories[].isLeaf").description("리프(최하위) 카테고리 여부"),
                subsectionWithPath("categories[].children").description("하위 카테고리 목록 (상위 구조와 동일)")
        };
    }

    public static FieldDescriptor[] getCategoryDetailResponse() {
        return new FieldDescriptor[] {
                ID, NAME, DEPTH,
                fieldWithPath("isLeaf").description("리프(최하위) 카테고리 여부"),
                subsectionWithPath("breadcrumb").description("루트부터 현재 카테고리까지의 경로")
        };
    }

    public static FieldDescriptor[] getUpdateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("카테고리 이름").optional().type(JsonFieldType.STRING),
                IMAGE_PATH
        };
    }

    public static FieldDescriptor[] getMoveCategoryRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("newParentId").description("이동할 부모 카테고리 ID").optional()
        };
    }
}
