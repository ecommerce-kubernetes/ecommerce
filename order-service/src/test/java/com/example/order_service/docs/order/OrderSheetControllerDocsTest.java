package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderSheetDescriptor;
import com.example.order_service.order.api.web.OrderSheetController;
import com.example.order_service.order.api.web.dto.request.CartOrderSheetCreateRequest;
import com.example.order_service.order.api.web.dto.request.DirectOrderSheetCreateRequest;
import com.example.order_service.order.api.web.dto.request.OrderSheetRequest;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateCartOrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResultDeprecate;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderSheetControllerDocsTest extends RestDocSupport {
    private OrderSheetService orderSheetService = mock(OrderSheetService.class);

    @Override
    protected Object initController() {
        return new OrderSheetController(orderSheetService);
    }

    @Test
    @DisplayName("주문서 즉시 생성")
    void createDirectOrderSheet() throws Exception {
        //given
        DirectOrderSheetCreateRequest.OrderVariant variant = DirectOrderSheetCreateRequest.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        DirectOrderSheetCreateRequest request = DirectOrderSheetCreateRequest.builder()
                .items(List.of(variant))
                .build();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetCreateResult result = createOrderSheetCreateResult();
        given(orderSheetService.createDirectOrderSheet(any(CreateDirectOrderSheetCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/order-sheets/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "order-sheets/create/direct",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(OrderSheetDescriptor.getDirectCreateRequest()),
                        responseFields(OrderSheetDescriptor.getCreateOrderSheetResponse())
                ));
    }

    @Test
    @DisplayName("장바구니 주문서 생성")
    void createCartOrderSheet() throws Exception {
        //given
        CartOrderSheetCreateRequest request = CartOrderSheetCreateRequest.builder()
                .cartItemIds(List.of(1L))
                .build();

        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetCreateResult result = createOrderSheetCreateResult();
        given(orderSheetService.createCartOrderSheet(any(CreateCartOrderSheetCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/order-sheets/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleUser)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "order-sheets/create/cart",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(OrderSheetDescriptor.getCartCreateRequest()),
                        responseFields(OrderSheetDescriptor.getCreateOrderSheetResponse())
                ));
    }

    @Test
    @DisplayName("주문서를 조회한다")
    void getOrderSheet() throws Exception {
        //given
        String orderSheetId = "orderSheetId";
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetResult result = createOrderSheetResult();
        given(orderSheetService.getOrderSheet(anyString(), anyLong())).willReturn(result);
        //when
        //then
        mockMvc.perform(get("/order-sheets/{orderSheetId}", orderSheetId)
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
                        responseFields(getOrderSheetResult()),
                        pathParameters(
                                parameterWithName("orderSheetId")
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
                        responseFields(getOrderSheetResult()),
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
                        responseFields(getOrderSheetResult()),
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
                        responseFields(getOrderSheetResult()),
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
                        responseFields(getOrderSheetResult()),
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

    private OrderSheetResultDeprecate.PaymentSummary createPaymentSummary() {
        return OrderSheetResultDeprecate.PaymentSummary.builder()
                .totalOriginPrice(Money.wons(10000L))
                .totalProductDiscount(Money.wons(1000L))
                .totalCouponDiscount(Money.wons(2000L))
                .usedPoints(Money.wons(1000L))
                .totalPaymentAmount(Money.wons(6000L))
                .build();
    }

    private OrderSheetResultDeprecate.Point createPoint() {
        return OrderSheetResultDeprecate.Point.builder()
                .ownedPoints(Money.wons(10000L))
                .availablePoints(Money.wons(4000L))
                .usedPoints(Money.wons(1000L))
                .build();
    }

    private List<OrderSheetResultDeprecate.OrderItem> createItems() {
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(2L, "하의 1000원 할인", Money.wons(1000L));
        List<ProductOptionSnapshot> productOptionSnapshots = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        return List.of(
                OrderSheetResultDeprecate.OrderItem.builder()
                        .sheetItemId("sheetItemId")
                        .productId(1L)
                        .productVariantId(1L)
                        .productName("청바지")
                        .thumbnail("/product/product/jean_1.jpg")
                        .quantity(1)
                        .productPrice(productPriceSnapshot)
                        .lineTotal(Money.wons(8000L))
//                        .appliedItemCoupon(itemCoupon)
                        .options(productOptionSnapshots)
                        .build()
        );
    }

    private OrderSheetRequest.UpdateUsedPoints createUpdatePointsOrderSheetRequest() {
        return OrderSheetRequest.UpdateUsedPoints.builder()
                .usedPoints(1000L)
                .build();
    }

    private OrderSheetResult createOrderSheetResult() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1_XL_BLUE", "청바지", "product/product/jean.jpg");
        OrderSheetResult.ItemPriceResult price = OrderSheetResult.ItemPriceResult.builder()
                .unitOriginalPrice(Money.wons(10000L))
                .unitDiscountedPrice(Money.wons(9000L))
                .lineTotal(Money.wons(9000L))
                .finalAmount(Money.wons(8000L))
                .build();
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("색상", "BLUE");
        OrderSheetResult.ItemCouponResult itemCoupon = OrderSheetResult.ItemCouponResult.builder()
                .itemCouponId(1L)
                .name("청바지 1000원 할인 쿠폰")
                .appliedDiscountAmount(Money.wons(1000L))
                .build();
        OrderSheetResult.CartCouponResult cartCoupon = OrderSheetResult.CartCouponResult.builder()
                .cartCouponId(2L)
                .name("장바구니 1000원 할인")
                .appliedDiscountAmount(Money.wons(1000L))
                .build();
        OrderSheetResult.OrderSheetItemResult item = OrderSheetResult.OrderSheetItemResult.builder()
                .orderSheetItemId("orderSheetItemId")
                .quantity(1)
                .product(product)
                .price(price)
                .options(List.of(option1, option2))
                .coupon(itemCoupon)
                .build();
        OrderSheetResult.PaymentSummaryResult paymentSummary = OrderSheetResult.PaymentSummaryResult.builder()
                .totalOriginalAmount(Money.wons(10000L))
                .totalItemDiscount(Money.wons(1000L))
                .totalItemCouponDiscount(Money.wons(1000L))
                .usedPoints(Money.wons(1000L))
                .cartCouponDiscount(Money.wons(1000L))
                .totalPaymentAmount(Money.wons(6000L))
                .build();
        return OrderSheetResult.builder()
                .orderSheetId("orderSheetId")
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(item))
                .cartCoupon(cartCoupon)
                .paymentSummary(paymentSummary)
                .expiresAt(LocalDateTime.now())
                .build();
    }

    private OrderSheetCreateResult createOrderSheetCreateResult() {
        return OrderSheetCreateResult.builder()
                .orderSheetId("orderSheetId")
                .expiresAt(LocalDateTime.now())
                .build();
    }
}
