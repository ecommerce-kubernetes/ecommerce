package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CartDescriptor {

    public static FieldDescriptor[] getAddCartItemRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].productVariantId").description("상품 변형 아이디"),
                fieldWithPath("items[].quantity").description("추가 수량")
        };
    }

    public static FieldDescriptor[] getAddCartItemResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                fieldWithPath("items[].status").description("장바구니 상품 상태[주문 가능, 삭제됨, 준비중]"),
                fieldWithPath("items[].isAvailable").description("주문 가능 여부"),
                fieldWithPath("items[].productId").description("상품 ID(상품 식별자)"),
                fieldWithPath("items[].productVariantId").description("상품 변형 ID"),
                fieldWithPath("items[].productName").description("상품 이름"),
                fieldWithPath("items[].thumbnail").description("상품 썸네일"),
                fieldWithPath("items[].quantity").description("수량"),
                fieldWithPath("items[].price.originalPrice").description("상품 원본 가격"),
                fieldWithPath("items[].price.discountRate").description("상품 할인율"),
                fieldWithPath("items[].price.discountAmount").description("상품 할인 금액"),
                fieldWithPath("items[].price.discountedPrice").description("할인된 가격"),
                fieldWithPath("items[].lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                fieldWithPath("items[].options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("items[].options[].optionValueName").description("상품 옵션 값 (예: XL)")
        };
    }
    
    public static FieldDescriptor[] getCartResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].id").description("장바구니 상품 ID(장바구니 상품 식별자)"),
                fieldWithPath("items[].status").description("장바구니 상품 상태[주문 가능, 삭제됨, 준비중]"),
                fieldWithPath("items[].isAvailable").description("주문 가능 여부"),
                fieldWithPath("items[].productId").description("상품 ID(상품 식별자)"),
                fieldWithPath("items[].productVariantId").description("상품 변형 ID"),
                fieldWithPath("items[].productName").description("상품 이름"),
                fieldWithPath("items[].thumbnail").description("상품 썸네일"),
                fieldWithPath("items[].quantity").description("수량"),
                fieldWithPath("items[].price.originalPrice").description("상품 원본 가격"),
                fieldWithPath("items[].price.discountRate").description("상품 할인율"),
                fieldWithPath("items[].price.discountAmount").description("상품 할인 금액"),
                fieldWithPath("items[].price.discountedPrice").description("할인된 가격"),
                fieldWithPath("items[].lineTotal").description("항목 총액 (상품 할인 가격 X 수량)"),
                fieldWithPath("items[].options[].optionTypeName").description("상품 옵션 타입 (예: 사이즈)"),
                fieldWithPath("items[].options[].optionValueName").description("상품 옵션 값 (예: XL)")
        };
    }
}
