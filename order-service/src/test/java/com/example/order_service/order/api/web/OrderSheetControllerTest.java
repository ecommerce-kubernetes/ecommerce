package com.example.order_service.order.api.web;

import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.order.api.web.dto.ordersheet.request.*;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.ApplyPointCommand;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateCartOrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.command.UpdateOrderSheetShippingAddressCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = OrderSheetController.class)
class OrderSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderSheetService orderSheetService;

    @Test
    @DisplayName("바로 구매 주문서 생성")
    @WithCustomMockUser
    void createDirectOrderSheet() throws Exception {
        //given
        DirectOrderSheetCreateRequest.OrderVariant item = DirectOrderSheetCreateRequest.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        DirectOrderSheetCreateRequest request = DirectOrderSheetCreateRequest.builder()
                .items(List.of(item))
                .build();

        OrderSheetCreateResult result = Instancio.create(OrderSheetCreateResult.class);
        given(orderSheetService.createDirectOrderSheet(any(CreateDirectOrderSheetCommand.class)))
                .willReturn(result);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);
        //when
        //then
        mockMvc.perform(post("/order-sheets/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 주문서를 생성할 수 없다.")
    void createDirectOrderSheet_unAuthorized() throws Exception {
        //given
        DirectOrderSheetCreateRequest.OrderVariant item = DirectOrderSheetCreateRequest.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        DirectOrderSheetCreateRequest request = DirectOrderSheetCreateRequest.builder()
                .items(List.of(item))
                .build();
        //when
        //then
        mockMvc.perform(post("/order-sheets/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/direct"));
    }

    @Test
    @DisplayName("사용자 권한이 부족하면 주문서를 생성할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void createDirectOrderSheet_forbidden() throws Exception {
        //given
        DirectOrderSheetCreateRequest.OrderVariant item = DirectOrderSheetCreateRequest.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();
        DirectOrderSheetCreateRequest request = DirectOrderSheetCreateRequest.builder()
                .items(List.of(item))
                .build();
        //when
        //then
        mockMvc.perform(post("/order-sheets/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/direct"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCreateDirectOrderSheetRequest")
    @WithCustomMockUser
    @DisplayName("주문서 즉시 생성 요청 검증")
    void createDirectOrderSheet_validation(String description, DirectOrderSheetCreateRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/order-sheets/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/direct"));
    }

    @Test
    @DisplayName("장바구니 주문서 생성")
    @WithCustomMockUser
    void createCartOrderSheet() throws Exception {
        //given
        CartOrderSheetCreateRequest request = CartOrderSheetCreateRequest.builder()
                .cartItemIds(List.of(1L))
                .build();

        OrderSheetCreateResult result = Instancio.create(OrderSheetCreateResult.class);

        given(orderSheetService.createCartOrderSheet(any(CreateCartOrderSheetCommand.class)))
                .willReturn(result);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);
        //when
        //then
        mockMvc.perform(post("/order-sheets/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 주문서를 생성할 수 없다.")
    void createCartOrderSheet_unAuthorized() throws Exception {
        //given
        CartOrderSheetCreateRequest request = CartOrderSheetCreateRequest.builder()
                .cartItemIds(List.of(1L))
                .build();
        //when
        //then
        mockMvc.perform(post("/order-sheets/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/cart"));
    }

    @Test
    @DisplayName("사용자 권한이 부족하면 주문서를 생성할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void createCartOrderSheet_forbidden() throws Exception {
        //given
        CartOrderSheetCreateRequest request = CartOrderSheetCreateRequest.builder()
                .cartItemIds(List.of(1L))
                .build();
        //when
        //then
        mockMvc.perform(post("/order-sheets/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/cart"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCreateCartOrderSheetRequest")
    @WithCustomMockUser
    @DisplayName("장바구니 주문서 생성 요청 검증")
    void createCartOrderSheet_validation(String description, CartOrderSheetCreateRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/order-sheets/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/cart"));
    }

    @Test
    @DisplayName("주문서를 조회한다")
    @WithCustomMockUser
    void getOrderSheet() throws Exception {
        //given
        Long orderSheetId = 1L;
        Long userId = 1L;
        OrderSheetResult result = Instancio.create(OrderSheetResult.class);
        given(orderSheetService.getOrderSheet(orderSheetId, userId))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/order-sheets/{orderSheetId}", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 주문서를 조회할 수 없다")
    void getOrderSheet_unAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(get("/order-sheets/{orderSheetId}", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId));
    }

    @Test
    @DisplayName("유저 권한이 아니면 주문서를 조회할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void getOrderSheet_forbidden() throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(get("/order-sheets/{orderSheetId}", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId));
    }

    @Test
    @DisplayName("배송 정보를 수정한다")
    @WithCustomMockUser
    void updateShippingAddress() throws Exception {
        //given
        Long orderSheetId = 1L;
        UpdateOrderSheetShippingAddressRequest request = UpdateOrderSheetShippingAddressRequest.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        OrderSheetUpdateResult result = Instancio.create(OrderSheetUpdateResult.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);

        given(orderSheetService.updateShippingAddress(any(UpdateOrderSheetShippingAddressCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/shipping-address", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 배송 정보를 수정할 수 없다")
    void updateShippingAddress_unAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        UpdateOrderSheetShippingAddressRequest request = UpdateOrderSheetShippingAddressRequest.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/shipping-address", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/shipping-address"));
    }

    @Test
    @DisplayName("사용자 권한이 아니면 배송정보를 수정할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void updateShippingAddress_forbidden() throws Exception {
        //given
        Long orderSheetId = 1L;
        UpdateOrderSheetShippingAddressRequest request = UpdateOrderSheetShippingAddressRequest.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/shipping-address", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/shipping-address"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("배송 정보 수정 입력 검증 테스트")
    @MethodSource("provideInvalidUpdateShippingAddressRequest")
    @WithCustomMockUser
    void updateShippingAddress_validate(String description, UpdateOrderSheetShippingAddressRequest req, String expectedField, String expectedMessage) throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/shipping-address"));
    }

    @Test
    @DisplayName("상품 쿠폰을 변경한다")
    @WithCustomMockUser
    void applyItemCoupon() throws Exception {
        //given
        Long orderSheetId = 1L;
        Long orderSheetItemId = 100L;

        ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest applyItemCoupon = ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest.builder()
                .orderSheetItemId(orderSheetItemId)
                .itemCouponId(1L)
                .build();

        ApplyOrderSheetItemCouponsRequest request = ApplyOrderSheetItemCouponsRequest.builder()
                .applyItemCoupons(List.of(applyItemCoupon))
                .build();

        OrderSheetUpdateResult result = Instancio.create(OrderSheetUpdateResult.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);

        given(orderSheetService.applyItemCoupons(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/item-coupons", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }


    @Test
    @DisplayName("로그인하지 않은 사용자는 상품 쿠폰을 변경할 수 없다")
    void applyItemCoupon_auAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        Long orderSheetItemId = 100L;

        ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest applyItemCoupon = ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest.builder()
                .orderSheetItemId(orderSheetItemId)
                .itemCouponId(1L)
                .build();

        ApplyOrderSheetItemCouponsRequest request = ApplyOrderSheetItemCouponsRequest.builder()
                .applyItemCoupons(List.of(applyItemCoupon))
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/item-coupons", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/item-coupons"));
    }

    @Test
    @DisplayName("유저 권한이 아니면 상품 쿠폰을 변경할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void applyItemCoupon_forbidden() throws Exception {
        //given
        Long orderSheetId = 1L;
        Long orderSheetItemId = 100L;

        ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest applyItemCoupon = ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest.builder()
                .orderSheetItemId(orderSheetItemId)
                .itemCouponId(1L)
                .build();

        ApplyOrderSheetItemCouponsRequest request = ApplyOrderSheetItemCouponsRequest.builder()
                .applyItemCoupons(List.of(applyItemCoupon))
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/item-coupons", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/item-coupons"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("상품 쿠폰 적용 요청 검증")
    @MethodSource("provideInvalidApplyItemCouponRequest")
    @WithCustomMockUser
    void applyItemCoupon_validation(String description, ApplyOrderSheetItemCouponsRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/item-coupons", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/item-coupons"));
    }

    @Test
    @DisplayName("장바구니 쿠폰을 변경한다")
    @WithCustomMockUser
    void applyCartCoupon() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetCartCouponRequest request = ApplyOrderSheetCartCouponRequest.builder()
                .cartCouponId(1L)
                .build();
        OrderSheetUpdateResult result = Instancio.create(OrderSheetUpdateResult.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);

        given(orderSheetService.applyCartCoupon(any())).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/" + orderSheetId + "/cart-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }

    @Test
    @DisplayName("로그인 되지 않은 사용자는 장바구니 쿠폰을 변경할 수 없다")
    void applyCartCoupon_unAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetCartCouponRequest request = ApplyOrderSheetCartCouponRequest.builder()
                .cartCouponId(1L)
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/" + orderSheetId + "/cart-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/cart-coupon"));
    }

    @Test
    @DisplayName("유저 권한이 아니면 장바구니 쿠폰을 변경할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void applyCartCoupon_forbidden() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetCartCouponRequest request = ApplyOrderSheetCartCouponRequest.builder()
                .cartCouponId(1L)
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/" + orderSheetId + "/cart-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/cart-coupon"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("장바구니 쿠폰 변경 요청 검증")
    @MethodSource("provideInvalidApplyCartCouponRequest")
    @WithCustomMockUser
    void applyCartCoupon_validation(String description, ApplyOrderSheetCartCouponRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(patch("/order-sheets/" + orderSheetId + "/cart-coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/cart-coupon"));
    }

    @Test
    @DisplayName("사용 포인트를 수정한다")
    @WithCustomMockUser
    void applyPoints() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetPointRequest request = ApplyOrderSheetPointRequest.builder()
                .usedPoints(1000L)
                .build();
        OrderSheetUpdateResult result = Instancio.create(OrderSheetUpdateResult.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedExpiresAt = result.expiresAt().format(formatter);

        given(orderSheetService.applyPoints(any(ApplyPointCommand.class))).willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/points", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderSheetId").value(result.orderSheetId()))
                .andExpect(jsonPath("$.expiresAt").value(expectedExpiresAt));
    }


    @Test
    @DisplayName("로그인되지 않은 사용자는 사용 포인트를 수정할 수 없다")
    void applyPoints_unAuthorized() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetPointRequest request = ApplyOrderSheetPointRequest.builder()
                .usedPoints(1000L)
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/points", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/points"));
    }

    @Test
    @DisplayName("유저 권한이 아니라면 포인트를 수정할 수 없다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void applyPoints_forbidden() throws Exception {
        //given
        Long orderSheetId = 1L;
        ApplyOrderSheetPointRequest request = ApplyOrderSheetPointRequest.builder()
                .usedPoints(1000L)
                .build();
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/points", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/points"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidApplyPointRequest")
    @DisplayName("사용 포인트 수정 입력 검증 테스트")
    @WithCustomMockUser
    void applyPoints_validate(String description, ApplyOrderSheetPointRequest req, String expectedField, String expectedMessage) throws Exception {
        //given
        Long orderSheetId = 1L;
        //when
        //then
        mockMvc.perform(patch("/order-sheets/{orderSheetId}/points", orderSheetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId + "/points"));
    }

    private static Stream<Arguments> provideInvalidCreateDirectOrderSheetRequest() {
        return Stream.of(
                Arguments.of(
                        "주문 상품이 없으면 검증에 실패한다",
                        DirectOrderSheetCreateRequest.builder()
                                .build(),
                        "items",
                        "주문 상품은 한개 이상이여야 합니다."
                ),
                Arguments.of(
                        "주문 상품이 한개 미만이면 검증에 실패한다",
                        DirectOrderSheetCreateRequest.builder()
                                .items(Collections.emptyList())
                                .build(),
                        "items",
                        "주문 상품은 한개 이상이여야 합니다."
                ),
                Arguments.of(
                        "주문 상품 식별자가 없으면 검증에 실패한다",
                        DirectOrderSheetCreateRequest.builder()
                                .items(
                                        List.of(
                                                DirectOrderSheetCreateRequest.OrderVariant.builder()
                                                        .productVariantId(null)
                                                        .quantity(1)
                                                        .build()
                                        )
                                )
                                .build(),
                        "items[0].productVariantId",
                        "상품 식별자(productVariantId)는 필수값입니다."
                ),
                Arguments.of(
                        "주문 상품 수량이 없으면 검증에 실패한다.",
                        DirectOrderSheetCreateRequest.builder()
                                .items(
                                        List.of(
                                                DirectOrderSheetCreateRequest.OrderVariant.builder()
                                                        .productVariantId(1L)
                                                        .quantity(null)
                                                        .build()
                                        )
                                )
                                .build(),
                        "items[0].quantity",
                        "수량(quantity)은 필수값입니다."
                ),
                Arguments.of(
                        "주문 상품 수량이 0이하면 검증에 실패한다",
                        DirectOrderSheetCreateRequest.builder()
                                .items(
                                        List.of(
                                                DirectOrderSheetCreateRequest.OrderVariant.builder()
                                                        .productVariantId(1L)
                                                        .quantity(0)
                                                        .build()
                                        )
                                )
                                .build(),
                        "items[0].quantity",
                        "수량(quantity)은 1개 이상이어야 합니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidCreateCartOrderSheetRequest() {
        return Stream.of(
                Arguments.of(
                        "장바구니 항목 아이디가 없으면 검증에 실패한다",
                        CartOrderSheetCreateRequest.builder()
                                .cartItemIds(null)
                                .build(),
                        "cartItemIds",
                        "장바구니 항목은 한개 이상이여야 합니다."
                ),
                Arguments.of(
                        "장바구니 항목 아이디 빈 경우 없으면 검증에 실패한다",
                        CartOrderSheetCreateRequest.builder()
                                .cartItemIds(Collections.emptyList())
                                .build(),
                        "cartItemIds",
                        "장바구니 항목은 한개 이상이여야 합니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidUpdateShippingAddressRequest() {
        return Stream.of(
                Arguments.of(
                        "수령인이 없으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName(null)
                                .receiverPhone("010-1234-5678")
                                .zipCode("12345")
                                .address("서울시 테헤란로 123")
                                .addressDetail("123동 1234호")
                                .build(),
                        "receiverName",
                        "수령인 이름은 필수입니다."
                ),
                Arguments.of(
                        "수령인 전화번호가 없으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName("수령인")
                                .receiverPhone(null)
                                .zipCode("12345")
                                .address("서울시 테헤란로 123")
                                .addressDetail("123동 1234호")
                                .build(),
                        "receiverPhone",
                        "수령인 전화번호는 필수입니다."
                ),
                Arguments.of(
                        "수령인 전화번호 형식이 올바르지 않으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName("수령인")
                                .receiverPhone("1234")
                                .zipCode("12345")
                                .address("서울시 테헤란로 123")
                                .addressDetail("123동 1234호")
                                .build(),
                        "receiverPhone",
                        "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
                ),
                Arguments.of(
                        "우편 번호가 없으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName("수령인")
                                .receiverPhone("010-1234-5678")
                                .zipCode(null)
                                .address("서울시 테헤란로 123")
                                .addressDetail("123동 1234호")
                                .build(),
                        "zipCode",
                        "우편 번호는 필수입니다."
                ),
                Arguments.of(
                        "기본 주소가 없으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName("수령인")
                                .receiverPhone("010-1234-5678")
                                .zipCode("12345")
                                .address(null)
                                .addressDetail("123동 1234호")
                                .build(),
                        "address",
                        "기본 주소는 필수입니다."
                ),
                Arguments.of(
                        "상세 주소가 없으면 검증에 실패한다",
                        UpdateOrderSheetShippingAddressRequest.builder()
                                .receiverName("수령인")
                                .receiverPhone("010-1234-5678")
                                .zipCode("12345")
                                .address("서울시 테헤란로 123")
                                .addressDetail(null)
                                .build(),
                        "addressDetail",
                        "상세 주소는 필수입니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidApplyItemCouponRequest() {
        return Stream.of(
                Arguments.of(
                        "적용 상품 쿠폰이 없으면 예외가 발생한다.",
                        ApplyOrderSheetItemCouponsRequest.builder()
                                .applyItemCoupons(Collections.emptyList())
                                .build(),
                        "applyItemCoupons",
                        "적용할 상품 쿠폰은 한 개 이상이어야 합니다."
                ),
                Arguments.of(
                        "주문 항목 아이디가 누락되면 예외가 발생한다.",
                        ApplyOrderSheetItemCouponsRequest.builder()
                                .applyItemCoupons(
                                        List.of(ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest.builder()
                                                .orderSheetItemId(null)
                                                .itemCouponId(1L)
                                                .build())
                                ).build(),
                        "applyItemCoupons[0].orderSheetItemId",
                        "주문 항목(OrderSheetItem) 식별자는 필수 입니다."
                ),
                Arguments.of(
                        "상품 쿠폰 아이디가 누락되면 예외가 발생한다.",
                        ApplyOrderSheetItemCouponsRequest.builder()
                                .applyItemCoupons(
                                        List.of(ApplyOrderSheetItemCouponsRequest.ApplyItemCouponRequest.builder()
                                                .orderSheetItemId(1L)
                                                .itemCouponId(null)
                                                .build())
                                ).build(),
                        "applyItemCoupons[0].itemCouponId",
                        "상품 쿠폰 식별자는 필수값 입니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidApplyCartCouponRequest() {
        return Stream.of(
                Arguments.of(
                        "장바구니 쿠폰 아이디가 없으면 검증에 실패한다",
                        ApplyOrderSheetCartCouponRequest.builder()
                                .cartCouponId(null)
                                .build(),
                        "cartCouponId",
                        "장바구니 쿠폰 식별자는 필수값 입니다."
                )
        );
    }

    private static Stream<Arguments> provideInvalidApplyPointRequest() {
        return Stream.of(
                Arguments.of(
                        "포인트가 없으면 검증에 실패한다",
                        ApplyOrderSheetPointRequest.builder()
                                .usedPoints(null).build(),
                        "usedPoints",
                        "사용 포인트는 필수입니다."
                ),
                Arguments.of(
                        "포인트 0가 0 미만이면 검증에 실패한다",
                        ApplyOrderSheetPointRequest.builder()
                                .usedPoints(-1L).build(),
                        "usedPoints",
                        "사용 포인트는 0이상이어야 합니다."
                )
        );
    }
}