package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class OptionDescriptor {
    public static FieldDescriptor[] createOptionTypeRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("values")
                        .type(JsonFieldType.ARRAY)
                        .description("옵션 값 리스트")
                        .attributes(key("constraint").value("필수, 최소 한개의 옵션 값 필요")),
                fieldWithPath("values[].name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 이름")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] createOptionTypeResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("optionTypeId")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 ID")
        };
    }

    public static FieldDescriptor[] optionTypesResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("optionTypes")
                        .type(JsonFieldType.ARRAY)
                        .description("옵션 타입 리스트"),
                fieldWithPath("optionTypes[].id")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 ID"),
                fieldWithPath("optionTypes[].name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 이름"),
                fieldWithPath("optionTypes[].values")
                        .type(JsonFieldType.ARRAY)
                        .description("옵션 값 리스트"),
                fieldWithPath("optionTypes[].values[].id")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 ID"),
                fieldWithPath("optionTypes[].values[].name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 이름"),
        };
    }

    public static FieldDescriptor[] optionTypeResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 ID"),
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 이름"),
                fieldWithPath("values")
                        .type(JsonFieldType.ARRAY)
                        .description("옵션 값 목록"),
                fieldWithPath("values[].id")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 아이디"),
                fieldWithPath("values[].name")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 이름")
        };
    }

    public static FieldDescriptor[] updateOptionTypeRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("변경할 옵션 타입 이름")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] updateOptionTypeResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("optionTypeId")
                        .type(JsonFieldType.STRING)
                        .description("옵션 타입 Id")
        };
    }

    public static FieldDescriptor[] addOptionValueRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("추가할 옵션 값 이름")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] addOptionValueResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("optionValueId")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 ID")
        };
    }

    public static FieldDescriptor[] updateOptionValueRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name")
                        .type(JsonFieldType.STRING)
                        .description("수정할 옵션 값 이름")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] updateOptionValueResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("optionValueId")
                        .type(JsonFieldType.STRING)
                        .description("옵션 값 ID")
        };
    }
}
