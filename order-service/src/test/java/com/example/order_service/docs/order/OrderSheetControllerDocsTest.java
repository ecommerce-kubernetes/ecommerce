package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderSheetDescriptor;
import com.example.order_service.order.api.OrderSheetController;
import com.example.order_service.order.api.dto.request.OrderSheetRequest;
import com.example.order_service.order.api.dto.response.OrderSheetResponse;
import com.example.order_service.order.application.service.ordersheet.OrderSheetAppService;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderSheetControllerDocsTest extends RestDocSupport {
    private OrderSheetAppService orderSheetAppService = mock(OrderSheetAppService.class);

    @Override
    protected String getTag() {
        return "ORDER-SHEETS";
    }

    @Override
    protected Object initController() {
        return new OrderSheetController(orderSheetAppService);
    }

    @Nested
    @DisplayName("주문서 생성 API")
    class CreateOrderSheet {
        @Test
        @DisplayName("주문서 생성 API")
        void createOrderSheet() throws Exception {
            //given
            OrderSheetRequest.OrderItem item = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(2)
                    .build();
            OrderSheetRequest.ItemCoupon itemCoupon = OrderSheetRequest.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .build();
            OrderSheetRequest.Create request = OrderSheetRequest.Create.builder()
                    .items(List.of(item))
                    .cartCouponId(1L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
            OrderSheetResult.Create result = createOrderSheetResult();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            given(orderSheetAppService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                    .willReturn(result);
            OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(createSecuredDocument("04-ordersheet-01-create",
                            "주문서 저장",
                            "주문서를 저장한다",
                            OrderSheetDescriptor.getCreateRequest(),
                            OrderSheetDescriptor.getCreateResponse()));
        }

        private OrderSheetResult.Create createOrderSheetResult() {
            return OrderSheetResult.Create.builder()
                    .sheetId("sheetId")
                    .expiresAt(LocalDateTime.now())
                    .build();
        }
    }

    @Nested
    @DisplayName("주문서 조회 API")
    class GetOrderSheet {
        @Test
        @DisplayName("주문서를 조회한다")
        void getOrderSheet() throws Exception {
            //given
            String orderSheetId = "sheetId";
            OrderSheetResult.Detail result = createOrderSheetResult();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            given(orderSheetAppService.getOrderSheet(anyString(), anyLong()))
                    .willReturn(result);
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(get("/order-sheets/{sheetId}", orderSheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(
                            createSecuredDocument("04-ordersheet-02-get",
                                    "주문서 조회",
                                    "주문서를 조회한다",
                                    OrderSheetDescriptor.getDetailResponse(),
                                    parameterWithName("sheetId").description("조회 주문서 아이디"))
                    );
        }
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {
        @Test
        @DisplayName("배송 정보를 수정한다")
        void updateShippingAddress() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateShippingAddress request = createOrderSheetRequest();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            OrderSheetResult.Detail result = createOrderSheetResult();
            given(orderSheetAppService.updateShippingAddress(any(OrderSheetCommand.UpdateShippingAddress.class)))
                    .willReturn(result);
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(
                            createSecuredDocument("04-ordersheet-03-update-shipping-address",
                                    "배송 정보 수정",
                                    "배송 정보를 수정한다",
                                    OrderSheetDescriptor.getShippingAddressRequest(),
                                    OrderSheetDescriptor.getDetailResponse(),
                                    parameterWithName("sheetId").description("주문서 아이디"))
                    );
        }

        private OrderSheetRequest.UpdateShippingAddress createOrderSheetRequest() {
            return OrderSheetRequest.UpdateShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
        }
    }

    @Nested
    @DisplayName("포인트 수정")
    class UpdatePoints {

        @Test
        @DisplayName("사용 포인트를 수정한다")
        void updatePoints() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateUsedPoints request = createOrderSheetRequest();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            OrderSheetResult.Detail result = createOrderSheetResult();
            given(orderSheetAppService.updatePoints(any())).willReturn(result);
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/points", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(
                            createSecuredDocument("04-ordersheet-04-update-points",
                                    "사용 포인트 수정",
                                    "사용 포인트를 수정한다",
                                    OrderSheetDescriptor.getUpdatePointsRequest(),
                                    OrderSheetDescriptor.getDetailResponse(),
                                    parameterWithName("sheetId").description("주문서 아이디"))
                    );
        }

        private OrderSheetRequest.UpdateUsedPoints createOrderSheetRequest() {
            return OrderSheetRequest.UpdateUsedPoints.builder()
                    .usedPoints(1000L)
                    .build();
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 변경")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 변경한다")
        void updateItemCoupon() throws Exception {
            //given
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            OrderSheetRequest.UpdateCoupon request = createRequest();
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            OrderSheetResult.Detail result = createOrderSheetResult();
            given(orderSheetAppService.updateItemCoupon(any())).willReturn(result);
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/sheet-items/{sheetItemId}/coupon", sheetId, sheetItemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(
                            createSecuredDocument("04-ordersheet-05-update-item-coupon",
                                    "상품 쿠폰 수정",
                                    "상품 쿠폰을 수정한다",
                                    OrderSheetDescriptor.getUpdateCouponRequest(),
                                    OrderSheetDescriptor.getDetailResponse(),
                                    parameterWithName("sheetId").description("주문서 아이디"),
                                    parameterWithName("sheetItemId").description("주문 상품 아이디"))
                    );
        }

        private OrderSheetRequest.UpdateCoupon createRequest(){
            return OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(2L)
                    .build();
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰을 변경한다")
        void updateCartCoupon() throws Exception {
            //given
            String sheetId = "sheetId";
            HttpHeaders roleUser = createAuthHeader("ROLE_USER");
            OrderSheetRequest.UpdateCoupon request = createRequest();
            OrderSheetResult.Detail result = createOrderSheetResult();
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            given(orderSheetAppService.updateCartCoupon(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/cart-coupon", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(roleUser)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)))
                    .andDo(
                            createSecuredDocument("04-ordersheet-06-update-cart-coupon",
                                    "장바구니 쿠폰 수정",
                                    "장바구니 쿠폰을 수정한다",
                                    OrderSheetDescriptor.getUpdateCouponRequest(),
                                    OrderSheetDescriptor.getDetailResponse(),
                                    parameterWithName("sheetId").description("주문서 아이디"))
                    );
        }

        private OrderSheetRequest.UpdateCoupon createRequest() {
            return OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
        }
    }

    private OrderSheetResult.Detail createOrderSheetResult() {
        return OrderSheetResult.Detail.builder()
                .sheetId("sheetId")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .orderer(createOrderer())
                .shippingAddress(createShippingAddress())
                .items(createItems())
                .cartCoupon(createCartCoupon())
                .point(createPoint())
                .paymentSummary(createPaymentSummary())
                .build();
    }

    private OrderSheetResult.OrdererInfo createOrderer() {
        return OrderSheetResult.OrdererInfo.builder()
                .userId(1L)
                .userName("주문자")
                .phoneNumber("010-1234-5678")
                .build();
    }

    private OrderSheetResult.ShippingInfo createShippingAddress() {
        return OrderSheetResult.ShippingInfo.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
    }

    private OrderSheetResult.PaymentSummary createPaymentSummary() {
        return OrderSheetResult.PaymentSummary.builder()
                .totalOriginPrice(Money.wons(10000L))
                .totalProductDiscount(Money.wons(1000L))
                .totalCouponDiscount(Money.wons(2000L))
                .usedPoints(Money.wons(1000L))
                .totalPaymentAmount(Money.wons(6000L))
                .build();
    }

    private OrderSheetResult.Point createPoint() {
        return OrderSheetResult.Point.builder()
                .ownedPoints(Money.wons(10000L))
                .availablePoints(Money.wons(4000L))
                .usedPoints(Money.wons(1000L))
                .build();
    }

    private List<OrderSheetResult.OrderItem> createItems() {
        return List.of(
                OrderSheetResult.OrderItem.builder()
                        .sheetItemId("sheetItemId")
                        .productId(1L)
                        .productVariantId(1L)
                        .productName("청바지")
                        .thumbnail("/product/product/jean_1.jpg")
                        .quantity(1)
                        .unitPrice(createItemPrice())
                        .lineTotal(Money.wons(8000L))
                        .appliedItemCoupon(createItemCoupon())
                        .options(createItemOption())
                        .build()
        );
    }

    private OrderSheetResult.OrderItemPrice createItemPrice() {
        return OrderSheetResult.OrderItemPrice.builder()
                .originalPrice(Money.wons(10000L))
                .discountRate(10)
                .discountAmount(Money.wons(1000L))
                .discountedPrice(Money.wons(9000L))
                .build();
    }

    private OrderSheetResult.Coupon createItemCoupon() {
        return OrderSheetResult.Coupon.builder()
                .couponId(2L)
                .couponName("하의 1000원 할인")
                .discountAmount(Money.wons(1000L))
                .build();
    }

    private OrderSheetResult.Coupon createCartCoupon() {
        return OrderSheetResult.Coupon.builder()
                .couponId(1L)
                .couponName("첫 구매 1000원 할인")
                .discountAmount(Money.wons(1000L))
                .build();
    }

    private List<OrderSheetResult.OrderItemOption> createItemOption() {
        return List.of(
                OrderSheetResult.OrderItemOption.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build(),
                OrderSheetResult.OrderItemOption.builder()
                        .optionTypeName("색상")
                        .optionValueName("BLUE")
                        .build()
        );
    }
}
