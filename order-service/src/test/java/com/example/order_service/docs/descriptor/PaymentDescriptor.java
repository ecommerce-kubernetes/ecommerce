package com.example.order_service.docs.descriptor;

import kafka.utils.Json;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class PaymentDescriptor {

    public static FieldDescriptor[] getConfirmRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderId")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 식별자")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("paymentKey")
                        .type(JsonFieldType.STRING)
                        .description("결제 키")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("amount")
                        .type(JsonFieldType.NUMBER)
                        .description("결제 금액")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] getApprovalResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("paymentId")
                        .type(JsonFieldType.NUMBER)
                        .description("결제 번호"),
        };
    }
}
