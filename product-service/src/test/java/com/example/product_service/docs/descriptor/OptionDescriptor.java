package com.example.product_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OptionDescriptor {
    public static final FieldDescriptor TYPE_ID = fieldWithPath("id").description("옵션 타입 아이디");
    public static final FieldDescriptor TYPE_NAME = fieldWithPath("name").description("옵션 이름");
    public static final FieldDescriptor VALUES_ID = fieldWithPath("values[].id").description("옵션 값 ID");
    public static final FieldDescriptor VALUES_NAME = fieldWithPath("values[].name").description("옵션 값");
    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("name").description("옵션 이름"),
                fieldWithPath("values[].name").description("옵션 값 이름")
        };
    }

    public static FieldDescriptor[] getOptionResponse() {
        return new FieldDescriptor[] {
                TYPE_ID, TYPE_NAME, VALUES_ID, VALUES_NAME
        };
    }

    public static FieldDescriptor[] getOptionListResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("[].id").description("옵션 타입 아이디"),
                fieldWithPath("[].name").description("옵션 이름"),
                fieldWithPath("[].values[].id").description("옵션 값 ID"),
                fieldWithPath("[].values[].name").description("옵션 값")
        };
    }

    public static FieldDescriptor[] getOptionUpdateRequest(){
        return new FieldDescriptor[]{
            fieldWithPath("name").description("변경할 이름")
        };
    }

    public static FieldDescriptor[] getOptionValueUpdateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("id").description("옵션 값 ID"),
                fieldWithPath("name").description("옵션 값 이름")
        };
    }
}
