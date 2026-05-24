package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderDescriptor {

    public static FieldDescriptor[] getOrderCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderSheetId").description("주문서 ID")
        };
    }

    public static FieldDescriptor[] getOrderCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("createdAt").description("주문 일시"),
                fieldWithPath("orderName").description("주문 설명"),
                fieldWithPath("finalPaymentAmount").description("최종 결제 금액")
        };
    }
}
