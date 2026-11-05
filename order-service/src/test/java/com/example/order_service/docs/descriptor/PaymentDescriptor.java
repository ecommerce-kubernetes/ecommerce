package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class PaymentDescriptor {

    public static FieldDescriptor[] createRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderId")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 식별자")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] confirmRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("paymentKey")
                        .type(JsonFieldType.STRING)
                        .description("결제 키")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("amount")
                        .type(JsonFieldType.NUMBER)
                        .description("결제 금액")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("provider")
                        .type(JsonFieldType.STRING)
                        .description("결제사")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] confirmResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("paymentId")
                        .type(JsonFieldType.NUMBER)
                        .description("결제 번호"),
        };
    }

    public static FieldDescriptor[] createResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("paymentId")
                        .type(JsonFieldType.STRING)
                        .description("결제 식별자")
        };
    }
}
