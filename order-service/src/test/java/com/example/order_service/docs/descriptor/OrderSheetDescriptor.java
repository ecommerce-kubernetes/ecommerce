package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class OrderSheetDescriptor {

    public static FieldDescriptor[] directCreateRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("items[].productVariantId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 변형 ID(상품 판매 단위 식별자)")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("items[].quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 수량")
                        .attributes(key("constraint").value("필수, 1이상"))
        };
    }

    public static FieldDescriptor[] cartCreateRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("cartItemIds")
                        .type(JsonFieldType.ARRAY)
                        .description("장바구니 항목 ID 리스트")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] shippingAddressRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("receiverName")
                        .type(JsonFieldType.STRING)
                        .description("수령인 이름")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("receiverPhone")
                        .type(JsonFieldType.STRING)
                        .description("수령인 전화번호")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("zipCode")
                        .type(JsonFieldType.STRING)
                        .description("우편 번호")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("address")
                        .type(JsonFieldType.STRING)
                        .description("기본 주소")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("addressDetail")
                        .type(JsonFieldType.STRING)
                        .description("상세 주소")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] applyItemCouponRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("applyItemCoupons[].orderSheetItemId")
                        .type(JsonFieldType.NUMBER)
                        .description("적용 주문 항목 ID")
                        .attributes(key("constraint").value("필수")),
                fieldWithPath("applyItemCoupons[].itemCouponId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 쿠폰 ID")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] applyCartCouponRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("cartCouponId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 쿠폰 ID")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] applyPointRequest() {
        return new FieldDescriptor[]{
                fieldWithPath("usedPoints")
                        .type(JsonFieldType.NUMBER)
                        .description("사용 포인트")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] orderSheetResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("orderSheetId")
                        .type(JsonFieldType.STRING)
                        .description("주문서 ID(주문서 식별자)"),
                fieldWithPath("orderer.userId")
                        .type(JsonFieldType.NUMBER)
                        .description("주문자 ID"),
                fieldWithPath("orderer.userName")
                        .type(JsonFieldType.STRING)
                        .description("주문자 이름"),
                fieldWithPath("orderer.phoneNumber")
                        .type(JsonFieldType.STRING)
                        .description("주문자 전화번호"),
                fieldWithPath("shippingAddress.receiverName")
                        .type(JsonFieldType.STRING)
                        .description("수령인 이름"),
                fieldWithPath("shippingAddress.receiverPhone")
                        .type(JsonFieldType.STRING)
                        .description("수령인 전화번호"),
                fieldWithPath("shippingAddress.zipCode")
                        .type(JsonFieldType.STRING)
                        .description("우편 번호"),
                fieldWithPath("shippingAddress.address")
                        .type(JsonFieldType.STRING)
                        .description("기본 주소"),
                fieldWithPath("shippingAddress.addressDetail")
                        .type(JsonFieldType.STRING)
                        .description("상세 주소"),
                fieldWithPath("items[*].orderSheetItemId")
                        .type(JsonFieldType.STRING)
                        .description("주문서 아이템 아이디"),
                fieldWithPath("items[*].product.productId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 아이디"),
                fieldWithPath("items[*].product.productVariantId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 변형 아이디"),
                fieldWithPath("items[*].product.sku")
                        .type(JsonFieldType.STRING)
                        .description("SKU"),
                fieldWithPath("items[*].product.productName")
                        .type(JsonFieldType.STRING)
                        .description("상품 이름"),
                fieldWithPath("items[*].product.thumbnail")
                        .type(JsonFieldType.STRING)
                        .description("상품 썸네일"),
                fieldWithPath("items[*].quantity")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 수량"),
                fieldWithPath("items[*].options[*].optionTypeName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 타입"),
                fieldWithPath("items[*].options[*].optionValueName")
                        .type(JsonFieldType.STRING)
                        .description("상품 옵션 값"),
                fieldWithPath("items[*].price.unitOriginalPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 원 가격"),
                fieldWithPath("items[*].price.unitDiscountedPrice")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 판매 가격"),
                fieldWithPath("items[*].price.lineTotal")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 항목 판매가 총액"),
                fieldWithPath("items[*].price.finalItemAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("주문 항목 최종 금액"),
                fieldWithPath("items[*].coupon.itemCouponId")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 쿠폰 ID"),
                fieldWithPath("items[*].coupon.name")
                        .type(JsonFieldType.STRING)
                        .description("상품 쿠폰 이름"),
                fieldWithPath("items[*].coupon.appliedDiscountAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("상품 쿠폰 할인 금액"),
                fieldWithPath("cartCoupon.cartCouponId")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 쿠폰 ID"),
                fieldWithPath("cartCoupon.name")
                        .type(JsonFieldType.STRING)
                        .description("장바구니 쿠폰 이름"),
                fieldWithPath("cartCoupon.appliedDiscountAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 쿠폰 할인 금액"),
                fieldWithPath("paymentSummary.totalOriginalAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("총 주문 항목 상품 원 금액"),
                fieldWithPath("paymentSummary.totalItemDiscount")
                        .type(JsonFieldType.NUMBER)
                        .description("총 상품 할인 금액"),
                fieldWithPath("paymentSummary.totalItemCouponDiscount")
                        .type(JsonFieldType.NUMBER)
                        .description("총 상품 쿠폰 할인 금액"),
                fieldWithPath("paymentSummary.cartCouponDiscount")
                        .type(JsonFieldType.NUMBER)
                        .description("장바구니 쿠폰 할인 금액"),
                fieldWithPath("paymentSummary.usedPoints")
                        .type(JsonFieldType.NUMBER)
                        .description("사용 포인트"),
                fieldWithPath("paymentSummary.totalPaymentAmount")
                        .type(JsonFieldType.NUMBER)
                        .description("총 결제 금액"),
                fieldWithPath("point.availablePoints")
                        .type(JsonFieldType.NUMBER)
                        .description("사용 가능 포인트"),
                fieldWithPath("point.maxUsablePoints")
                        .type(JsonFieldType.NUMBER)
                        .description("최대 적용 가능 포인트"),
                fieldWithPath("expiresAt").description("주문서 만료 시간")
        };
    }

    public static FieldDescriptor[] createOrderSheetResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("orderSheetId")
                        .type(JsonFieldType.STRING)
                        .description("주문서 ID"),
                fieldWithPath("expiresAt")
                        .type(JsonFieldType.STRING)
                        .description("주문서 만료 시간")
        };
    }

    public static FieldDescriptor[] updateOrderSheetResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("orderSheetId")
                        .type(JsonFieldType.STRING)
                        .description("주문서 ID"),
                fieldWithPath("expiresAt")
                        .type(JsonFieldType.STRING)
                        .description("주문서 만료 시간")
        };
    }
}
