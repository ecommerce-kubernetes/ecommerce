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

    public static FieldDescriptor[] orderCreateResponse() {
        return new FieldDescriptor[] {
            fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("주문 식별자")
        };
    }

    public static FieldDescriptor[] orderResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                fieldWithPath("status").type(JsonFieldType.STRING).description("주문 상태"),
                fieldWithPath("orderName").type(JsonFieldType.STRING).description("주문 이름"),
                fieldWithPath("orderer.userId").type(JsonFieldType.NUMBER).description("주문자 아이디"),
                fieldWithPath("orderer.userName").type(JsonFieldType.STRING).description("주문자 이름"),
                fieldWithPath("orderer.phoneNumber").type(JsonFieldType.STRING).description("주문자 전화번호"),
                fieldWithPath("shippingAddress.receiverName").type(JsonFieldType.STRING).description("수령인 이름"),
                fieldWithPath("shippingAddress.receiverPhone").type(JsonFieldType.STRING).description("수령인 전화번호"),
                fieldWithPath("shippingAddress.zipCode").type(JsonFieldType.STRING).description("배송 우편 번호"),
                fieldWithPath("shippingAddress.address").type(JsonFieldType.STRING).description("배송지 정보"),
                fieldWithPath("shippingAddress.addressDetail").type(JsonFieldType.STRING).description("배송지 상세 정보"),
                fieldWithPath("orderItems[].orderItemId").type(JsonFieldType.STRING).description("주문 상품 식별자"),
                fieldWithPath("orderItems[].product.productId").type(JsonFieldType.NUMBER).description("주문 상품 아이디"),
                fieldWithPath("orderItems[].product.productVariantId").type(JsonFieldType.NUMBER).description("주문 상품 변형 아이디"),
                fieldWithPath("orderItems[].product.sku").type(JsonFieldType.STRING).description("주문 상품 SKU"),
                fieldWithPath("orderItems[].product.productName").type(JsonFieldType.STRING).description("주문 상품 이름"),
                fieldWithPath("orderItems[].product.thumbnail").type(JsonFieldType.STRING).description("주문 상품 썸네일"),
                fieldWithPath("orderItems[].options[].optionTypeName").type(JsonFieldType.STRING).description("주문 상품 옵션 타입"),
                fieldWithPath("orderItems[].options[].optionValueName").type(JsonFieldType.STRING).description("주문 상품 옵션 이름"),
                fieldWithPath("orderItems[].quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                fieldWithPath("orderItems[].orderItemAmount.originalAmount").type(JsonFieldType.NUMBER).description("주문 항목 원 가격"),
                fieldWithPath("orderItems[].orderItemAmount.itemDiscount").type(JsonFieldType.NUMBER).description("주문 항목 상품 할인 가격"),
                fieldWithPath("orderItems[].orderItemAmount.lineTotal").type(JsonFieldType.NUMBER).description("주문 항목 상품 판매가"),
                fieldWithPath("orderItems[].orderItemAmount.couponDiscount").type(JsonFieldType.NUMBER).description("상품 쿠폰 할인 금액"),
                fieldWithPath("orderItems[].orderItemAmount.finalItemAmount").type(JsonFieldType.NUMBER).description("최종 주문 항목 가격"),
                fieldWithPath("orderAmount.totalOriginalAmount").type(JsonFieldType.NUMBER).description("총 상품 금액"),
                fieldWithPath("orderAmount.totalItemDiscount").type(JsonFieldType.NUMBER).description("총 상품 할인 금액"),
                fieldWithPath("orderAmount.totalItemCouponDiscount").type(JsonFieldType.NUMBER).description("총 상품 쿠폰 할인 금액"),
                fieldWithPath("orderAmount.cartCouponDiscount").type(JsonFieldType.NUMBER).description("장바구니 쿠폰 할인 금액"),
                fieldWithPath("orderAmount.usedPoints").type(JsonFieldType.NUMBER).description("사용 포인트"),
                fieldWithPath("orderAmount.totalPaymentAmount").type(JsonFieldType.NUMBER).description("총 결제 금액"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("주문 생성일")
        };
    }

    public static FieldDescriptor[] orderSummaryResponse() {
        return new FieldDescriptor[] {
                fieldWithPath("content[].orderId").type(JsonFieldType.STRING).description("주문 번호"),
                fieldWithPath("content[].status").type(JsonFieldType.STRING).description("주문 상태"),
                fieldWithPath("content[].orderItems[].orderItemId").type(JsonFieldType.STRING).description("주문 상품 식별자"),
                fieldWithPath("content[].orderItems[].product.productId").type(JsonFieldType.NUMBER).description("주문 상품 아이디"),
                fieldWithPath("content[].orderItems[].product.productVariantId").type(JsonFieldType.NUMBER).description("주문 상품 변형 아이디"),
                fieldWithPath("content[].orderItems[].product.sku").type(JsonFieldType.STRING).description("주문 상품 SKU"),
                fieldWithPath("content[].orderItems[].product.productName").type(JsonFieldType.STRING).description("주문 상품 이름"),
                fieldWithPath("content[].orderItems[].product.thumbnail").type(JsonFieldType.STRING).description("주문 상품 썸네일"),
                fieldWithPath("content[].orderItems[].itemPayment.lineTotal").type(JsonFieldType.NUMBER).description("주문 항목 상품 판매가 총액"),
                fieldWithPath("content[].orderItems[].itemPayment.couponDiscount").type(JsonFieldType.NUMBER).description("상품 쿠폰 할인 금액"),
                fieldWithPath("content[].orderItems[].itemPayment.finalItemAmount").type(JsonFieldType.NUMBER).description("최종 주문 항목 가격 총액"),
                fieldWithPath("content[].orderItems[].quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                fieldWithPath("content[].orderItems[].options[].optionTypeName").type(JsonFieldType.STRING).description("주문 상품 옵션 타입"),
                fieldWithPath("content[].orderItems[].options[].optionValueName").type(JsonFieldType.STRING).description("주문 상품 옵션 타입"),
                fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("주문 생성일"),
                fieldWithPath("currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                fieldWithPath("totalPage").type(JsonFieldType.NUMBER).description("총 페이지"),
                fieldWithPath("pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                fieldWithPath("totalElement").type(JsonFieldType.NUMBER).description("총 요소 개수")
        };
    }
}
