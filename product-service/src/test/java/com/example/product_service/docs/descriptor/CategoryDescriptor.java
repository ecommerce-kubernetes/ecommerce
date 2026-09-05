package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class CategoryDescriptor {
    public static FieldDescriptor[] createRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("parentId")
                        .type(JsonFieldType.NUMBER)
                        .description("부모 카테고리 ID (최상위 노드 생성 시 null 또는 생략 가능)")
                        .optional(),
                fieldWithPath("imagePath")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이미지 경로 (예: \"/categories/food.jpg\"")
                        .optional()
        };
    }

    public static FieldDescriptor[] createResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id")
                        .type(JsonFieldType.STRING)
                        .description("생성 카테고리 ID")
        };
    }

    public static FieldDescriptor[] updateRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름")
                        .attributes(key("constraint").value("최소 1개 필드 입력 필수"))
                        .optional(),
                fieldWithPath("imagePath")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이미지 경로 (예: \"/categories/food.jpg\")")
                        .attributes(key("constraint").value("최소 1개 필드 입력 필수"))
                        .optional()
        };
    }

    public static FieldDescriptor[] updateResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id")
                        .type(JsonFieldType.STRING)
                        .description("변경된 카테고리 ID")
        };
    }

    public static FieldDescriptor[] moveRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("newParentId")
                        .type(JsonFieldType.NUMBER)
                        .description("변경 부모 카테고리 ID  (최상위 노드로 변경시 null 또는 생략 가능)")
                        .optional()
        };
    }

    public static FieldDescriptor[] moveResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("id")
                        .type(JsonFieldType.STRING)
                        .description("변경된 카테고리 ID")
        };
    }

    public static FieldDescriptor[] rootsResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("categories")
                        .type(JsonFieldType.ARRAY)
                        .description("최상위 카테고리 목록"),
                fieldWithPath("categories[].id")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 ID"),
                fieldWithPath("categories[].name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름"),
                fieldWithPath("categories[].isLeaf")
                        .type(JsonFieldType.BOOLEAN)
                        .description("최하위 노드 여부 (true인 경우 하위 카테고리가 없음을 의미)"),
                fieldWithPath("categories[].imagePath")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이미지 경로 (이미지가 없는 하위 노드의 경우 null 반환)")
                        .optional()
        };
    }

    public static FieldDescriptor[] childResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("categories")
                        .type(JsonFieldType.ARRAY)
                        .description("하위 카테고리 목록"),
                fieldWithPath("categories[].id")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 ID"),
                fieldWithPath("categories[].name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름"),
                fieldWithPath("categories[].isLeaf")
                        .type(JsonFieldType.BOOLEAN)
                        .description("최하위 노드 여부 (true인 경우 하위 카테고리가 없음을 의미)"),
                fieldWithPath("categories[].imagePath")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이미지 경로 (이미지가 없는 하위 노드의 경우 null 반환)")
                        .optional()
        };
    }

    public static FieldDescriptor[] detailResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id")
                        .type(JsonFieldType.NUMBER)
                        .description("카테고리 ID"),
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름"),
                fieldWithPath("depth")
                        .type(JsonFieldType.NUMBER)
                        .description("카테고리 뎁스 (Depth)"),
                fieldWithPath("isLeaf")
                        .type(JsonFieldType.BOOLEAN)
                        .description("최하위 노드 여부 (true인 경우 하위 카테고리가 없음)"),
                fieldWithPath("breadcrumb")
                        .type(JsonFieldType.ARRAY)
                        .description("해당 카테고리의 전체 상위 경로 목록 (루트부터 자기 자신까지 순차 정렬)"),
                fieldWithPath("breadcrumb[].id")
                        .type(JsonFieldType.NUMBER)
                        .description("경로 내 카테고리 ID"),
                fieldWithPath("breadcrumb[].name")
                        .type(JsonFieldType.STRING)
                        .description("경로 내 카테고리 이름")
        };
    }

    public static FieldDescriptor[] treeResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("categories")
                        .type(JsonFieldType.ARRAY)
                        .description("전체 카테고리 트리 목록"),
                fieldWithPath("categories[].id")
                        .type(JsonFieldType.NUMBER)
                        .description("카테고리 ID"),
                fieldWithPath("categories[].name")
                        .type(JsonFieldType.STRING)
                        .description("카테고리 이름"),
                fieldWithPath("categories[].isLeaf")
                        .type(JsonFieldType.BOOLEAN)
                        .description("최하위 노드 여부 (true인 경우 하위 카테고리가 없음)"),
                subsectionWithPath("categories[].children")
                        .type(JsonFieldType.ARRAY)
                        .description("하위 카테고리 목록 (상위 카테고리 객체와 동일한 구조가 재귀적으로 반복됨)")
        };
    }
}
