package com.example.order_service.docs.descriptor;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class OrderDescriptor {

    public static FieldDescriptor[] orderCreateRequest() {
        return new FieldDescriptor[] {
                fieldWithPath("orderSheetId")
                        .type(JsonFieldType.NUMBER)
                        .description("주문서 ID(주문서 식별자)")
                        .attributes(key("constraint").value("필수"))
        };
    }

    public static FieldDescriptor[] getOrderCreateResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("createdAt").description("주문 일시"),
                fieldWithPath("orderName").description("주문 설명"),
                fieldWithPath("totalPaymentAmount").description("최종 결제 금액")
        };
    }

    public static FieldDescriptor[] getOrderDetailResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("orderNo").description("주문 번호"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("orderName").description("주문 이름"),
                fieldWithPath("orderer.userId").description("주문자 아이디"),
                fieldWithPath("orderer.userName").description("주문자 이름"),
                fieldWithPath("orderer.phoneNumber").description("주문자 전화번호"),
                fieldWithPath("shippingAddress.receiverName").description("수령인 이름"),
                fieldWithPath("shippingAddress.receiverPhone").description("수령인 전화번호"),
                fieldWithPath("shippingAddress.zipCode").description("배송 우편 번호"),
                fieldWithPath("shippingAddress.address").description("배송지 정보"),
                fieldWithPath("shippingAddress.addressDetail").description("배송지 상세 정보"),
                fieldWithPath("cartCoupon.couponId").description("장바구니 쿠폰 아이디"),
                fieldWithPath("cartCoupon.couponName").description("장바구니 쿠폰 이름"),
                fieldWithPath("cartCoupon.discountAmount").description("장바구니 쿠폰 할인 금액"),
                fieldWithPath("orderItems[].product.productId").description("주문 상품 아이디"),
                fieldWithPath("orderItems[].product.productVariantId").description("주문 상품 변형 아이디"),
                fieldWithPath("orderItems[].product.sku").description("주문 상품 SKU"),
                fieldWithPath("orderItems[].product.productName").description("주문 상품 이름"),
                fieldWithPath("orderItems[].product.thumbnail").description("주문 상품 썸네일"),
                fieldWithPath("orderItems[].price.originalPrice").description("주문 상품 원본 금액"),
                fieldWithPath("orderItems[].price.discountRate").description("주문 상품 할인율"),
                fieldWithPath("orderItems[].price.discountAmount").description("주문 상품 할인 금액"),
                fieldWithPath("orderItems[].price.discountedPrice").description("주문 상품 판매 금액"),
                fieldWithPath("orderItems[].itemCoupon.couponId").description("주문 상품 쿠폰 아이디"),
                fieldWithPath("orderItems[].itemCoupon.couponName").description("주문 상품 쿠폰 이름"),
                fieldWithPath("orderItems[].itemCoupon.discountAmount").description("주문 상품 쿠폰 할인 금액"),
                fieldWithPath("orderItems[].quantity").description("주문 수량"),
                fieldWithPath("orderItems[].optionSnapshots[].optionTypeName").description("주문 상품 옵션 타입"),
                fieldWithPath("orderItems[].optionSnapshots[].optionValueName").description("주문 상품 옵션 이름"),
                fieldWithPath("totalOriginalPrice").description("총 상품 금액"),
                fieldWithPath("totalProductDiscountAmount").description("총 상품 할인 금액"),
                fieldWithPath("totalCouponDiscountAmount").description("총 쿠폰 할인 금액"),
                fieldWithPath("usedPoints").description("사용 포인트"),
                fieldWithPath("totalPaymentAmount").description("총 결제 금액"),
                fieldWithPath("createdAt").description("주문 생성일")
        };
    }

    public static FieldDescriptor[] getOrderSummaryResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("content[].orderNo").description("주문 번호"),
                fieldWithPath("content[].orderName").description("주문 이름"),
                fieldWithPath("content[].status").description("상품 상태"),
                fieldWithPath("content[].orderItems[].product.productId").description("주문 상품 아이디"),
                fieldWithPath("content[].orderItems[].product.productVariantId").description("주문 상품 변형 아이디"),
                fieldWithPath("content[].orderItems[].product.sku").description("주문 상품 SKU"),
                fieldWithPath("content[].orderItems[].product.productName").description("주문 상품 이름"),
                fieldWithPath("content[].orderItems[].product.thumbnail").description("주문 상품 썸네일"),
                fieldWithPath("content[].orderItems[].price.originalPrice").description("주문 상품 원본 금액"),
                fieldWithPath("content[].orderItems[].price.discountRate").description("주문 상품 할인율"),
                fieldWithPath("content[].orderItems[].price.discountAmount").description("주문 상품 할인 금액"),
                fieldWithPath("content[].orderItems[].price.discountedPrice").description("주문 상품 판매 금액"),
                fieldWithPath("content[].orderItems[].itemCoupon.couponId").description("주문 상품 쿠폰 아이디"),
                fieldWithPath("content[].orderItems[].itemCoupon.couponName").description("주문 상품 쿠폰 이름"),
                fieldWithPath("content[].orderItems[].itemCoupon.discountAmount").description("주문 상품 쿠폰 할인 금액"),
                fieldWithPath("content[].orderItems[].quantity").description("주문 수량"),
                fieldWithPath("content[].orderItems[].optionSnapshots[].optionTypeName").description("주문 상품 옵션 타입"),
                fieldWithPath("content[].orderItems[].optionSnapshots[].optionValueName").description("주문 상품 옵션 이름"),
                fieldWithPath("content[].createdAt").description("주문 생성일"),
                fieldWithPath("currentPage").description("현재 페이지"),
                fieldWithPath("totalPage").description("총 페이지"),
                fieldWithPath("pageSize").description("페이지 크기"),
                fieldWithPath("totalElement").description("총 요소 개수")
        };
    }
}
