package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.api.web.OrderSheetController;
import com.example.order_service.order.api.web.dto.request.OrderSheetRequest;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.docs.descriptor.OrderSheetDescriptor.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderSheetControllerDocsTest extends RestDocSupport {
    private OrderSheetService orderSheetService = mock(OrderSheetService.class);

    @Override
    protected Object initController() {
        return new OrderSheetController(orderSheetService);
    }

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
        OrderSheetResult.Create result = createCreateOrderSheetResult();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderSheetService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/order-sheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document(
                        "order-sheets/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(getCreateRequest()),
                        responseFields(getCreateResponse())
                ));
    }

    @Test
    @DisplayName("주문서를 조회한다")
    void getOrderSheet() throws Exception {
        //given
        String orderSheetId = "sheetId";
        OrderSheetResult.Detail result = createOrderSheetResult();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        given(orderSheetService.getOrderSheet(anyString(), anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/order-sheets/{sheetId}", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser))
                .andExpect(status().isOk())
                .andDo(document(
                        "order-sheets/get",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getDetailResponse()),
                        pathParameters(
                                parameterWithName("sheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }


    @Test
    @DisplayName("배송 정보를 수정한다")
    void updateShippingAddress() throws Exception {
        //given
        String sheetId = "sheetId";
        OrderSheetRequest.UpdateShippingAddress request = createOrderSheetRequest();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetResult.Detail result = createOrderSheetResult();
        given(orderSheetService.updateShippingAddress(any(OrderSheetCommand.UpdateShippingAddress.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", sheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document(
                        "order-sheets/update/shipping-address",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(getShippingAddressRequest()),
                        responseFields(getDetailResponse()),
                        pathParameters(
                                parameterWithName("sheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }


    @Test
    @DisplayName("사용 포인트를 수정한다")
    void updatePoints() throws Exception {
        //given
        String sheetId = "sheetId";
        OrderSheetRequest.UpdateUsedPoints request = createUpdatePointsOrderSheetRequest();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetResult.Detail result = createOrderSheetResult();
        given(orderSheetService.updatePoints(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{sheetId}/points", sheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document(
                        "order-sheets/update/points",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(getUpdatePointsRequest()),
                        responseFields(getDetailResponse()),
                        pathParameters(
                                parameterWithName("sheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }


    @Test
    @DisplayName("상품 쿠폰을 변경한다")
    void updateItemCoupon() throws Exception {
        //given
        String sheetId = "sheetId";
        String sheetItemId = "sheetItemId";
        OrderSheetRequest.UpdateCoupon request = createItemCouponRequest();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetResult.Detail result = createOrderSheetResult();
        given(orderSheetService.updateItemCoupon(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{sheetId}/sheet-items/{sheetItemId}/coupon", sheetId, sheetItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document(
                        "order-sheets/update/item-coupon",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(getUpdateCouponRequest()),
                        responseFields(getDetailResponse()),
                        pathParameters(
                                parameterWithName("sheetId")
                                        .description("주문서 ID(주문서 식별자)"),
                                parameterWithName("sheetItemId")
                                        .description("주문서 상품 ID(주문서 상품 식별자)")
                        )
                ));
    }


    @Test
    @DisplayName("장바구니 쿠폰을 변경한다")
    void updateCartCoupon() throws Exception {
        //given
        String sheetId = "sheetId";
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetRequest.UpdateCoupon request = createCartCouponRequest();
        OrderSheetResult.Detail result = createOrderSheetResult();
        given(orderSheetService.updateCartCoupon(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{sheetId}/cart-coupon", sheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document(
                        "order-sheets/update/cart-coupon",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(getUpdateCouponRequest()),
                        responseFields(getDetailResponse()),
                        pathParameters(
                                parameterWithName("sheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }

    private OrderSheetRequest.UpdateCoupon createItemCouponRequest() {
        return OrderSheetRequest.UpdateCoupon.builder()
                .couponId(2L)
                .build();
    }

    private OrderSheetRequest.UpdateCoupon createCartCouponRequest() {
        return OrderSheetRequest.UpdateCoupon.builder()
                .couponId(1L)
                .build();
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

    private Orderer createOrderer() {
        return Orderer.of(1L, "주문자", "010-1234-5678");
    }

    private ShippingAddress createShippingAddress() {
        return ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
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
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(2L, "하의 1000원 할인", Money.wons(1000L));
        List<ProductOptionSnapshot> productOptionSnapshots = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        return List.of(
                OrderSheetResult.OrderItem.builder()
                        .sheetItemId("sheetItemId")
                        .productId(1L)
                        .productVariantId(1L)
                        .productName("청바지")
                        .thumbnail("/product/product/jean_1.jpg")
                        .quantity(1)
                        .productPrice(productPriceSnapshot)
                        .lineTotal(Money.wons(8000L))
                        .appliedItemCoupon(itemCoupon)
                        .options(productOptionSnapshots)
                        .build()
        );
    }

    private OrderSheetRequest.UpdateUsedPoints createUpdatePointsOrderSheetRequest() {
        return OrderSheetRequest.UpdateUsedPoints.builder()
                .usedPoints(1000L)
                .build();
    }

    private OrderSheetResult.Create createCreateOrderSheetResult() {
        return OrderSheetResult.Create.builder()
                .sheetId("sheetId")
                .expiresAt(LocalDateTime.now())
                .build();
    }

    private OrderSheetResult.Detail createOrderSheetResult() {
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "첫 구매 1000원 할인", Money.wons(1000L));
        return OrderSheetResult.Detail.builder()
                .sheetId("sheetId")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .orderer(createOrderer())
                .shippingAddress(createShippingAddress())
                .items(createItems())
                .cartCoupon(cartCoupon)
                .point(createPoint())
                .paymentSummary(createPaymentSummary())
                .build();
    }
}
