package com.example.order_service.docs.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.docs.descriptor.OrderSheetDescriptor;
import com.example.order_service.order.api.web.OrderSheetController;
import com.example.order_service.order.api.web.dto.request.*;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
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
    @DisplayName("바로 구매 주문서 생성")
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
                        requestFields(OrderSheetDescriptor.directCreateRequest()),
                        responseFields(OrderSheetDescriptor.createOrderSheetResponse())
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
                        requestFields(OrderSheetDescriptor.cartCreateRequest()),
                        responseFields(OrderSheetDescriptor.createOrderSheetResponse())
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
                        responseFields(orderSheetResponse()),
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
        String orderSheetId = "orderSheetId";
        UpdateOrderSheetShippingAddressRequest request = UpdateOrderSheetShippingAddressRequest.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetUpdateResult result = createOrderSheetUpdateResult();
        given(orderSheetService.updateShippingAddress(any(UpdateOrderSheetShippingAddressCommand.class)))
                .willReturn(result);

        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/shipping-address", orderSheetId)
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
                        requestFields(shippingAddressRequest()),
                        responseFields(updateOrderSheetResponse()),
                        pathParameters(
                                parameterWithName("orderSheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }

    @Test
    @DisplayName("상품 쿠폰을 변경한다")
    void updateItemCoupon() throws Exception {
        //given
        String orderSheetId = "orderSheetId";
        String orderSheetItemId = "orderSheetItemId";
        ApplyOrderSheetItemCouponRequest request = ApplyOrderSheetItemCouponRequest.builder()
                .itemCouponId(1L)
                .build();
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        OrderSheetUpdateResult result = createOrderSheetUpdateResult();
        given(orderSheetService.applyItemCoupon(any(ApplyItemCouponCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/items/{orderSheetItemId}/coupon", orderSheetId, orderSheetItemId)
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
                        requestFields(applyItemCouponRequest()),
                        responseFields(updateOrderSheetResponse()),
                        pathParameters(
                                parameterWithName("orderSheetId")
                                        .description("주문서 ID(주문서 식별자)"),
                                parameterWithName("orderSheetItemId")
                                        .description("주문서 상품 ID(주문서 상품 식별자)")
                        )
                ));
    }

    @Test
    @DisplayName("장바구니 쿠폰을 변경한다")
    void applyCartCoupon() throws Exception {
        //given
        String orderSheetId = "orderSheetId";
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        ApplyOrderSheetCartCouponRequest request = ApplyOrderSheetCartCouponRequest.builder()
                .cartCouponId(2L)
                .build();

        OrderSheetUpdateResult result = createOrderSheetUpdateResult();
        given(orderSheetService.applyCartCoupon(any(ApplyCartCouponCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/cart-coupon", orderSheetId)
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
                        requestFields(applyCartCouponRequest()),
                        responseFields(updateOrderSheetResponse()),
                        pathParameters(
                                parameterWithName("orderSheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
    }


    @Test
    @DisplayName("사용 포인트를 수정한다")
    void applyPoints() throws Exception {
        //given
        String orderSheetId = "orderSheetId";
        HttpHeaders roleUser = createAuthHeader("ROLE_USER");
        ApplyOrderSheetPointRequest request = ApplyOrderSheetPointRequest.builder()
                .usedPoints(1000L)
                .build();

        OrderSheetUpdateResult result = createOrderSheetUpdateResult();
        given(orderSheetService.applyPoints(any(ApplyPointCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/points", orderSheetId)
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
                        requestFields(applyPointRequest()),
                        responseFields(updateOrderSheetResponse()),
                        pathParameters(
                                parameterWithName("orderSheetId")
                                        .description("주문서 ID(주문서 식별자)")
                        )
                ));
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
        OrderSheetResult.AppliedItemCouponResult itemCoupon = OrderSheetResult.AppliedItemCouponResult.builder()
                .itemCouponId(1L)
                .name("청바지 1000원 할인 쿠폰")
                .appliedDiscountAmount(Money.wons(1000L))
                .build();
        OrderSheetResult.AppliedCartCouponResult cartCoupon = OrderSheetResult.AppliedCartCouponResult.builder()
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
        OrderSheetResult.PointResult pointResult = OrderSheetResult.PointResult.builder()
                .availablePoints(Money.wons(1000L))
                .maxUsablePoints(Money.wons(2500L))
                .build();
        return OrderSheetResult.builder()
                .orderSheetId("orderSheetId")
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(item))
                .cartCoupon(cartCoupon)
                .paymentSummary(paymentSummary)
                .point(pointResult)
                .expiresAt(LocalDateTime.now())
                .build();
    }

    private OrderSheetCreateResult createOrderSheetCreateResult() {
        return OrderSheetCreateResult.builder()
                .orderSheetId("orderSheetId")
                .expiresAt(LocalDateTime.now())
                .build();
    }

    private OrderSheetUpdateResult createOrderSheetUpdateResult() {
        return OrderSheetUpdateResult.builder()
                .orderSheetId("orderSheetId")
                .expiresAt(LocalDateTime.now())
                .build();
    }
}
