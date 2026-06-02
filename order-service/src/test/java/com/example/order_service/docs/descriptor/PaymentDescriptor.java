package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class PaymentDescriptor {

    public static FieldDescriptor[] getConfirmRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("paymentKey").description("결제 키"),
                fieldWithPath("amount").description("결제 금액")
        };
    }

    public static FieldDescriptor[] getApprovalResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("paymentKey").description("결제 키"),
                fieldWithPath("totalAmount").description("총 결제 금액"),
                fieldWithPath("method").description("결제 수단"),
                fieldWithPath("status").description("결제 상태"),
                fieldWithPath("approvedAt").description("결제 시간")
        };
    }
}
