package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderSheetDescriptor {

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].productVariantId").description("상품 변형 아이디"),
                fieldWithPath("items[].quantity").description("주문 수량")
        };
    }

    public static FieldDescriptor[] getCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("sheetId").description("주문서 id"),
                fieldWithPath("expiresAt").description("주문서 만료 시간")
        };
    }

    public static FieldDescriptor[] getDetailResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("sheetId").description("주문서 id"),
                fieldWithPath("expiresAt").description("주문서 만료 시간"),
                fieldWithPath("paymentSummary.totalOriginalPrice").description("총 상품 가격"),
                fieldWithPath("paymentSummary.totalProductDiscountAmount").description("총 상품 할인 가격"),
                fieldWithPath("paymentSummary.totalBasePaymentAmount").description("총 결제 가격"),
                fieldWithPath("userAssets.availablePoint").description("사용 가능 포인트"),
                fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                fieldWithPath("items[].productId").type(JsonFieldType.NUMBER).description("상품 ID"),
                fieldWithPath("items[].productVariantId").type(JsonFieldType.NUMBER).description("상품 변형 ID"),
                fieldWithPath("items[].productName").type(JsonFieldType.STRING).description("상품명"),
                fieldWithPath("items[].thumbnail").type(JsonFieldType.STRING).description("썸네일"),
                fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                fieldWithPath("items[].unitPrice.originalPrice").description("상품 원가"),
                fieldWithPath("items[].unitPrice.discountRate").description("할인율"),
                fieldWithPath("items[].unitPrice.discountAmount").description("할인 금액"),
                fieldWithPath("items[].unitPrice.discountedPrice").description("할인 적용된 가격"),
                fieldWithPath("items[].lineTotal").description("해당 상품의 총 주문 금액"),
                fieldWithPath("items[].options[].optionTypeName").description("옵션 타입명 (예: 색상)"),
                fieldWithPath("items[].options[].optionValueName").description("옵션 값 (예: RED)"),
                fieldWithPath("userAssets.coupons[].couponId").type(JsonFieldType.NUMBER).description("쿠폰 ID"),
                fieldWithPath("userAssets.coupons[].couponName").type(JsonFieldType.STRING).description("쿠폰 이름"),
                fieldWithPath("userAssets.coupons[].discountAmount").type(JsonFieldType.NUMBER).description("할인 금액"),
                fieldWithPath("userAssets.coupons[].expiresAt").type(JsonFieldType.STRING).description("만료 일시")

        };
    }
}
