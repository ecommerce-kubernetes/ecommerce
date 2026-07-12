package com.example.order_service.order.api.web;

import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.order.api.web.dto.request.OrderSheetRequest;
import com.example.order_service.order.application.service.ordersheet.OrderSheetService;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.example.order_service.support.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("주문서를 저장한다")
        @WithCustomMockUser
        void createOrderSheet() throws Exception {
            //given
            Long productVariantId = 1L;
            OrderSheetRequest.OrderItem item = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(1)
                    .build();
            OrderSheetRequest.ItemCoupon itemCoupon = OrderSheetRequest.ItemCoupon.builder()
                    .couponId(2L)
                    .productVariantId(productVariantId)
                    .build();
            OrderSheetRequest.Create request = OrderSheetRequest.Create.builder()
                    .items(List.of(item))
                    .cartCouponId(1L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();

            OrderSheetResult.Create result = Instancio.create(OrderSheetResult.Create.class);
            given(orderSheetService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문서를 저장할 수 없다")
        void createOrderSheet_unAuthorized() throws Exception {
            //given
            Long productVariantId = 1L;
            OrderSheetRequest.OrderItem item = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(1)
                    .build();
            OrderSheetRequest.ItemCoupon itemCoupon = OrderSheetRequest.ItemCoupon.builder()
                    .couponId(2L)
                    .productVariantId(productVariantId)
                    .build();
            OrderSheetRequest.Create request = OrderSheetRequest.Create.builder()
                    .items(List.of(item))
                    .cartCouponId(1L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @Test
        @DisplayName("유저 권한이 아니면 주문서를 저장할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void createOrderSheet_forbidden() throws Exception {
            //given
            Long productVariantId = 1L;
            OrderSheetRequest.OrderItem item = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(1)
                    .build();
            OrderSheetRequest.ItemCoupon itemCoupon = OrderSheetRequest.ItemCoupon.builder()
                    .couponId(2L)
                    .productVariantId(productVariantId)
                    .build();
            OrderSheetRequest.Create request = OrderSheetRequest.Create.builder()
                    .items(List.of(item))
                    .cartCouponId(1L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("주문서 저장 입력 검증 테스트")
        @MethodSource("provideInvalidCreateRequest")
        @WithCustomMockUser
        void createOrderSheet_validate(String description, OrderSheetRequest.Create req, String expectedField, String expectedMessage) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        private static Stream<Arguments> provideInvalidCreateRequest() {
            OrderSheetRequest.OrderItem VALID_BASE_ITEM = OrderSheetRequest.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(3)
                    .build();
            OrderSheetRequest.ItemCoupon VALID_BASE_ITEM_COUPON = OrderSheetRequest.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(1L)
                    .build();
            return Stream.of(
                    Arguments.of(
                            "주문 상품 리스트가 빈 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "items",
                            "주문 상품은 한개 이상이여야 합니다."
                    ),

                    Arguments.of(
                            "상품 변형 Id 가 없는 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().productVariantId(null).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "items[0].productVariantId",
                            "상품 식별자(productVariantId)는 필수값입니다."
                    ),

                    Arguments.of(
                            "수량이 없는 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(null).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "items[0].quantity",
                            "수량(quantity)은 필수값입니다."
                    ),
                    Arguments.of(
                            "수량이 0개 이하인 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(0).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "items[0].quantity",
                            "수량(quantity)은 1개 이상이어야 합니다."
                    ),
                    Arguments.of(
                            "상품 쿠폰이 null인 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(null)
                                    .build(),
                            "itemCoupons",
                            "상품 쿠폰은 필수값 입니다."
                    ),
                    Arguments.of(
                            "상품 쿠폰 사용 상품 식별자가 없는 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of(VALID_BASE_ITEM_COUPON.toBuilder()
                                            .productVariantId(null).build()))
                                    .build(),
                            "itemCoupons[0].productVariantId",
                            "쿠폰을 적용할 상품 식별자(productVariantId)는 필수값 입니다."
                    ),
                    Arguments.of(
                            "상품 쿠폰 아이디가 없는 경우 검증에 실패한다",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of(VALID_BASE_ITEM_COUPON.toBuilder()
                                            .couponId(null)
                                            .build()))
                                    .build(),
                            "itemCoupons[0].couponId",
                            "쿠폰 식별자(couponId)는 필수값 입니다."
                    )
            );
        }
    }

    @Nested
    @DisplayName("주문서 조회")
    class Get {

        @Test
        @DisplayName("주문서를 조회한다")
        @WithCustomMockUser
        void getOrderSheet() throws Exception {
            //given
            String orderSheetId = "sheetId";
            Long userId = 1L;
            OrderSheetResult.Detail result = Instancio.create(OrderSheetResult.Detail.class);
            given(orderSheetService.getOrderSheet(orderSheetId, userId))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(get("/order-sheets/{sheetId}", orderSheetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));

        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문서를 조회할 수 없다")
        void getOrderSheet_unAuthorized() throws Exception {
            //given
            String orderSheetId = "sheetId";
            //when
            //then
            mockMvc.perform(get("/order-sheets/{sheetId}", orderSheetId)
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
        void createOrderSheet_forbidden() throws Exception {
            //given
            String orderSheetId = "sheetId";
            //when
            //then
            mockMvc.perform(get("/order-sheets/{sheetId}", "sheetId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId));
        }
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송 정보를 수정한다")
        @WithCustomMockUser
        void updateShippingAddress() throws Exception {
            //given
            OrderSheetRequest.UpdateShippingAddress request = OrderSheetRequest.UpdateShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
            OrderSheetResult.Detail result = Instancio.create(OrderSheetResult.Detail.class);
            given(orderSheetService.updateShippingAddress(any(OrderSheetCommand.UpdateShippingAddress.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", "sheetId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 배송 정보를 수정할 수 없다")
        void updateShippingAddress_unAuthorized() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateShippingAddress request = OrderSheetRequest.UpdateShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", "sheetId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/shipping-address"));
        }

        @Test
        @DisplayName("사용자 권한이 아니면 배송정보를 수정할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updateShippingAddress_forbidden() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateShippingAddress request = OrderSheetRequest.UpdateShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/shipping-address"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("배송 정보 수정 입력 검증 테스트")
        @MethodSource("provideInvalidRequest")
        @WithCustomMockUser
        void updateShippingAddress_validate(String description, OrderSheetRequest.UpdateShippingAddress req, String expectedField, String expectedMessage) throws Exception {
            //given
            String sheetId = "sheetId";
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/shipping-address", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/shipping-address"));
        }

        private static Stream<Arguments> provideInvalidRequest() {
            OrderSheetRequest.UpdateShippingAddress VALID_REQUEST = OrderSheetRequest.UpdateShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
            return Stream.of(
                    Arguments.of(
                            "수령인이 없으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().receiverName(null).build(),
                            "receiverName",
                            "수령인 이름은 필수입니다."
                    ),
                    Arguments.of(
                            "수령인 전화번호가 없으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().receiverPhone(null).build(),
                            "receiverPhone",
                            "수령인 전화번호는 필수입니다."
                    ),
                    Arguments.of(
                            "수령인 전화번호 형식이 올바르지 않으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().receiverPhone("123123").build(),
                            "receiverPhone",
                            "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
                    ),
                    Arguments.of(
                            "우편 번호가 없으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().zipCode(null).build(),
                            "zipCode",
                            "우편 번호는 필수입니다."
                    ),
                    Arguments.of(
                            "기본 주소가 없으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().address(null).build(),
                            "address",
                            "기본 주소는 필수입니다."
                    ),
                    Arguments.of(
                            "상세 주소가 없으면 검증에 실패한다",
                            VALID_REQUEST.toBuilder().addressDetail(null).build(),
                            "addressDetail",
                            "상세 주소는 필수입니다."
                    )
            );
        }
    }

    @Nested
    @DisplayName("사용 포인트 수정")
    class UpdatePoints {

        @Test
        @DisplayName("사용 포인트를 수정한다")
        @WithCustomMockUser
        void updatePoints() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateUsedPoints request = OrderSheetRequest.UpdateUsedPoints.builder()
                    .usedPoints(1000L)
                    .build();
            OrderSheetResult.Detail result = Instancio.create(OrderSheetResult.Detail.class);
            given(orderSheetService.updatePoints(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/points", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));
        }

        @Test
        @DisplayName("로그인되지 않은 사용자는 사용 포인트를 수정할 수 없다")
        void updatePoints_unAuthorized() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateUsedPoints request = OrderSheetRequest.UpdateUsedPoints.builder()
                    .usedPoints(1000L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/points", "sheetId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/points"));
        }

        @Test
        @DisplayName("유저 권한이 아니라면 포인트를 수정할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updatePoints_forbidden() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateUsedPoints request = OrderSheetRequest.UpdateUsedPoints.builder()
                    .usedPoints(1000L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/points", "sheetId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/points"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideInvalidRequest")
        @DisplayName("사용 포인트 수정 입력 검증 테스트")
        @WithCustomMockUser
        void updatePoints_validate(String description, OrderSheetRequest.UpdateUsedPoints req, String expectedField, String expectedMessage) throws Exception {
            //given
            String sheetId = "sheetId";
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/points", sheetId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors[0].field").value(expectedField))
                    .andExpect(jsonPath("$.errors[0].reason").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/points"));
        }

        private static Stream<Arguments> provideInvalidRequest() {
            return Stream.of(
                    Arguments.of(
                            "포인트가 없으면 검증에 실패한다",
                            OrderSheetRequest.UpdateUsedPoints.builder()
                                    .usedPoints(null).build(),
                            "usedPoints",
                            "사용 포인트는 필수입니다."
                    ),
                    Arguments.of(
                            "포인트 0가 0 미만이면 검증에 실패한다",
                            OrderSheetRequest.UpdateUsedPoints.builder()
                                    .usedPoints(-1L).build(),
                            "usedPoints",
                            "사용 포인트는 0이상이어야 합니다."
                    )
            );
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 변경")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 변경한다")
        @WithCustomMockUser
        void updateItemCoupon() throws Exception {
            //given
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            OrderSheetResult.Detail result = Instancio.create(OrderSheetResult.Detail.class);
            given(orderSheetService.updateItemCoupon(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/sheet-items/{sheetItemId}/coupon", sheetId, sheetItemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));
        }

        @Test
        @DisplayName("로그인하지 않은 사용자는 상품 쿠폰을 변경할 수 없다")
        void updateItemCoupon_auAuthorized() throws Exception {
            //given
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/sheet-items/{sheetItemId}/coupon", sheetId, sheetItemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/sheet-items/" + sheetItemId + "/coupon"));
        }

        @Test
        @DisplayName("유저 권한이 아니면 상품 쿠폰을 변경할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updateItemCoupon_forbidden() throws Exception {
            //given
            String sheetId = "sheetId";
            String sheetItemId = "sheetItemId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/{sheetId}/sheet-items/{sheetItemId}/coupon", sheetId, sheetItemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/sheet-items/" + sheetItemId + "/coupon"));
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰을 변경한다")
        @WithCustomMockUser
        void updateCartCoupon() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            OrderSheetResult.Detail result = Instancio.create(OrderSheetResult.Detail.class);
            given(orderSheetService.updateCartCoupon(any())).willReturn(result);
            //when
            //then
            mockMvc.perform(patch("/order-sheets/" + sheetId + "/cart-coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sheetId").value(result.sheetId()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(result.items().size()))
                    .andExpect(jsonPath("$.orderer.userId").value(result.orderer().getUserId()))
                    .andExpect(jsonPath("$.paymentSummary.totalPaymentAmount").value(result.paymentSummary().totalPaymentAmount().longValue()));
        }

        @Test
        @DisplayName("로그인 되지 않은 사용자는 장바구니 쿠폰을 변경할 수 없다")
        void updateCartCoupon_unAuthorized() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/" + sheetId + "/cart-coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/cart-coupon"));
        }

        @Test
        @DisplayName("유저 권한이 아니면 장바구니 쿠폰을 변경할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void updateCartCoupon_forbidden() throws Exception {
            //given
            String sheetId = "sheetId";
            OrderSheetRequest.UpdateCoupon request = OrderSheetRequest.UpdateCoupon.builder()
                    .couponId(1L)
                    .build();
            //when
            //then
            mockMvc.perform(patch("/order-sheets/" + sheetId + "/cart-coupon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다."))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + sheetId + "/cart-coupon"));
        }
    }
}