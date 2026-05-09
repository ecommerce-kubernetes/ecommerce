package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderDescriptor {

    public static FieldDescriptor[] getOrderCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderSheetId").description("주문서 ID"),
                fieldWithPath("deliveryAddress.receiverName").description("수령인"),
                fieldWithPath("deliveryAddress.receiverPhone").description("수령인 전화번호"),
                fieldWithPath("deliveryAddress.zipCode").description("우편번호"),
                fieldWithPath("deliveryAddress.baseAddress").description("기본 주소"),
                fieldWithPath("deliveryAddress.detailAddress").description("상세 주소"),
                fieldWithPath("couponId").description("사용 쿠폰 Id").optional(),
                fieldWithPath("pointToUse").description("사용 포인트"),
                fieldWithPath("expectedPrice").description("예상 결제 금액")
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
