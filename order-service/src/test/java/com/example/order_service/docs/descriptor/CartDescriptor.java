package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class CartDescriptor {

    public static FieldDescriptor[] addCartItemsRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("items[].productVariantId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 변형 ID(상품 판매 단위 식별자)")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("items[].quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("추가 수량")
                        .attributes(key("constraint").value("필수, 1이상"))
        };
    }
    public static FieldDescriptor[] addCartItemsResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("items[].cartItemId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 ID(장바구니 항목 식별자)")
        };
    }

    public static FieldDescriptor[] cartResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("items[].cartItemId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 ID(장바구니 항목 식별자)"),
                fieldWithPath("items[].status")
                        .type(JsonFieldType.STRING)
                        .description("장바구니 항목 상태(주문 가능, 품절, 주문 불가)"),
                fieldWithPath("items[].productId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 ID(상품 식별자)"),
                fieldWithPath("items[].productVariantId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 변형 ID(상품 판매 단위 식별자)"),
                fieldWithPath("items[].productName")
                        .type(JsonFieldType.STRING)
                        .description("상품 이름"),
                fieldWithPath("items[].thumbnail")
                        .type(JsonFieldType.STRING)
                        .description("대표 상품 이미지"),
                fieldWithPath("items[].quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 수량"),

                fieldWithPath("items[].price.originalPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 원본 가격"),
                fieldWithPath("items[].price.discountRate")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 할인율"),
                fieldWithPath("items[].price.discountAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 할인 금액"),
                fieldWithPath("items[].price.discountedPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 판매 금액(상품 할인 금액 적용 가격)"),

                fieldWithPath("items[].lineTotal")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 총액 (상품 판매 금액 * 항목 수량)"),

                fieldWithPath("items[].options[].optionTypeName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 이름 (예: 사이즈)"),
                fieldWithPath("items[].options[].optionValueName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 값 (예: XL)"),

                fieldWithPath("totalCount")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 총 항목 개수")
        };
    }

    public static FieldDescriptor[] cartItemResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("cartItemId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 ID(장바구니 항목 식별자)"),
                fieldWithPath("status")
                        .type(JsonFieldType.STRING)
                        .description("장바구니 항목 상태(주문 가능, 품절, 주문 불가)"),
                fieldWithPath("productId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 ID(상품 식별자)"),
                fieldWithPath("productVariantId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 변형 ID(상품 판매 단위 식별자)"),
                fieldWithPath("productName")
                        .type(JsonFieldType.STRING)
                        .description("상품 이름"),
                fieldWithPath("thumbnail")
                        .type(JsonFieldType.STRING)
                        .description("대표 상품 이미지"),
                fieldWithPath("quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 수량"),

                fieldWithPath("price.originalPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 원본 가격"),
                fieldWithPath("price.discountRate")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 할인율"),
                fieldWithPath("price.discountAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 할인 금액"),
                fieldWithPath("price.discountedPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 판매 금액(상품 할인 금액 적용 가격)"),

                fieldWithPath("lineTotal")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 총액 (상품 판매 금액 * 항목 수량)"),

                fieldWithPath("options[].optionTypeName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 이름 (예: 사이즈)"),
                fieldWithPath("options[].optionValueName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 값 (예: XL)")
        };
    }

    public static FieldDescriptor[] updateCartItemQuantityRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("변경할 수량")
                        .attributes(key("constraint").value("필수, 1이상"))
        };
    }

    public static FieldDescriptor[] updateCartItemQuantityResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("cartItemId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 항목 ID(장바구니 항목 식별자)")
        };
    }
}
