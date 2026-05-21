package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderSheetDescriptor {

    public static FieldDescriptor[] getCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("items[].productVariantId").description("상품 변형 아이디"),
                fieldWithPath("items[].quantity").description("주문 수량"),
                fieldWithPath("cartCouponId").description("장바구니 쿠폰 아이디"),
                fieldWithPath("itemCoupons[].productVariantId").description("상품 쿠폰 적용 상품 아이디"),
                fieldWithPath("itemCoupons[].couponId").description("상품 쿠폰 아이디")
        };
    }

    public static FieldDescriptor[] getShippingAddressRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("receiverName").description("수령인 이름"),
                fieldWithPath("receiverPhone").description("수령인 전화번호"),
                fieldWithPath("zipCode").description("우편 번호"),
                fieldWithPath("address").description("기본 주소"),
                fieldWithPath("addressDetail").description("상세 주소")
        };
    }

    public static FieldDescriptor[] getUpdatePointsRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("usedPoints").description("사용 포인트")
        };
    }

    public static FieldDescriptor[] getUpdateCouponRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("couponId").description("쿠폰 아이디")
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
                fieldWithPath("orderer.userId").description("주문자 아이디"),
                fieldWithPath("orderer.userName").description("주문자 이름"),
                fieldWithPath("orderer.phoneNumber").description("주문자 전화번호"),
                fieldWithPath("shippingAddress.receiverName").description("수령인 이름"),
                fieldWithPath("shippingAddress.receiverPhone").description("수령인 전화번호"),
                fieldWithPath("shippingAddress.zipCode").description("우편 번호"),
                fieldWithPath("shippingAddress.address").description("주소"),
                fieldWithPath("shippingAddress.addressDetail").description("상세 주소"),
                fieldWithPath("items[*].sheetItemId").description("주문서 아이템 아이디"),
                fieldWithPath("items[*].productId").description("상품 아이디"),
                fieldWithPath("items[*].productVariantId").description("상품 변형 아이디"),
                fieldWithPath("items[*].productName").description("상품 이름"),
                fieldWithPath("items[*].thumbnail").description("상품 썸네일"),
                fieldWithPath("items[*].quantity").description("주문 수량"),
                fieldWithPath("items[*].unitPrice.originalPrice").description("상품 가격"),
                fieldWithPath("items[*].unitPrice.discountRate").description("상품 할인율"),
                fieldWithPath("items[*].unitPrice.discountAmount").description("상품 할인금"),
                fieldWithPath("items[*].unitPrice.discountedPrice").description("상품 판매 가격"),
                fieldWithPath("items[*].lineTotal").description("상품 총액"),
                fieldWithPath("items[*].appliedItemCoupon.couponId").description("상품 쿠폰 아이디"),
                fieldWithPath("items[*].appliedItemCoupon.couponName").description("사용 쿠폰 이름"),
                fieldWithPath("items[*].appliedItemCoupon.discountAmount").description("상품 쿠폰 할인금"),
                fieldWithPath("items[*].options[*].optionTypeName").description("상품 옵션 타입"),
                fieldWithPath("items[*].options[*].optionValueName").description("상품 옵션 값"),
                fieldWithPath("cartCoupon.couponId").description("장바구니 쿠폰 아이디"),
                fieldWithPath("cartCoupon.couponName").description("장바구니 쿠폰 이름"),
                fieldWithPath("cartCoupon.discountAmount").description("장바구니 쿠폰 할인금"),
                fieldWithPath("point.ownedPoints").description("보유 포인트"),
                fieldWithPath("point.availablePoints").description("사용 가능 포인트"),
                fieldWithPath("point.usedPoints").description("사용 포인트"),
                fieldWithPath("paymentSummary.totalOriginalPrice").description("총 상품 가격"),
                fieldWithPath("paymentSummary.totalProductDiscountAmount").description("총 상품 할인금"),
                fieldWithPath("paymentSummary.totalCouponDiscount").description("총 쿠폰 할인금"),
                fieldWithPath("paymentSummary.usedPoints").description("사용 포인트"),
                fieldWithPath("paymentSummary.totalPaymentAmount").description("총 결제 가격")
        };
    }
}
