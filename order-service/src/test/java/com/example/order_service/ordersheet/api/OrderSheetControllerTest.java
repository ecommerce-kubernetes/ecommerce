package com.example.order_service.ordersheet.api;

import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.ordersheet.api.dto.request.OrderSheetRequest;
import com.example.order_service.ordersheet.api.dto.response.OrderSheetResponse;
import com.example.order_service.ordersheet.application.OrderSheetAppService;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.nonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = OrderSheetController.class)
class OrderSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderSheetAppService orderSheetAppService;

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("주문서를 저장한다")
        @WithCustomMockUser
        void createOrderSheet() throws Exception {
            //given
            OrderSheetRequest.OrderItem item = fixtureMonkey.giveMeBuilder(OrderSheetRequest.OrderItem.class)
                    .set("productVariantId", 1L)
                    .sample();
            OrderSheetRequest.ItemCoupon itemCoupon = fixtureMonkey.giveMeBuilder(OrderSheetRequest.ItemCoupon.class)
                    .set("productVariantId", 1L)
                    .sample();
            OrderSheetRequest.Create request = fixtureMonkey.giveMeBuilder(OrderSheetRequest.Create.class)
                    .set("items", List.of(item))
                    .set("itemCoupons", List.of(itemCoupon))
                    .sample();
            OrderSheetResult.Create result = nonNull(fixtureMonkey.giveMeOne(OrderSheetResult.Create.class));
            given(orderSheetAppService.createOrderSheet(any(OrderSheetCommand.Create.class)))
                    .willReturn(result);
            OrderSheetResponse.Create response = OrderSheetResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문서를 저장할 수 없다")
        void createOrderSheet_unAuthorized() throws Exception {
            //given
            OrderSheetRequest.Create request = nonNull(fixtureMonkey.giveMeOne(OrderSheetRequest.Create.class));
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @Test
        @DisplayName("유저 권한이 아니면 주문서를 저장할 수 없다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void createOrderSheet_forbidden() throws Exception {
            //given
            OrderSheetRequest.Create request = nonNull(fixtureMonkey.giveMeOne(OrderSheetRequest.Create.class));
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("주문서 저장 입력 검증 테스트")
        @MethodSource("provideInvalidCreateRequest")
        @WithCustomMockUser
        void createOrderSheet_validate(String description, OrderSheetRequest.Create req, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(post("/order-sheets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
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
                            "상품이 없음",
                            OrderSheetRequest.Create.builder()
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "주문 상품은 한개 이상이여야 합니다"
                    ),
                    Arguments.of(
                            "상품 변형 id가 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().productVariantId(null).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "productVariantId는 필수값입니다"
                    ),
                    Arguments.of(
                            "수량이 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(null).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "quantity는 필수값입니다"
                    ),
                    Arguments.of(
                            "수량이 1미만",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM.toBuilder().quantity(0).build()))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of())
                                    .build(),
                            "quantity는 1이상 이여야 합니다"
                    ),
                    Arguments.of(
                            "상품 쿠폰이 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(null)
                                    .build(),
                            "상품 쿠폰은 필수값 입니다"
                    ),
                    Arguments.of(
                            "상품 쿠폰 사용 상품 variantId가 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of(VALID_BASE_ITEM_COUPON.toBuilder()
                                            .productVariantId(null).build()))
                                    .build(),
                            "쿠폰을 적용할 상품 변형 아이디는 필수값 입니다"
                    ),
                    Arguments.of(
                            "상품 쿠폰 아이디가 null",
                            OrderSheetRequest.Create.builder()
                                    .items(List.of(VALID_BASE_ITEM))
                                    .cartCouponId(null)
                                    .itemCoupons(List.of(VALID_BASE_ITEM_COUPON.toBuilder()
                                            .couponId(null)
                                            .build()))
                                    .build(),
                            "적용할 쿠폰 아이디는 필수값 입니다"
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
            Long userId = 1L;
            String orderSheetId = "sheetId";
            OrderSheetResult.Detail result = fixtureMonkey.giveMeOne(OrderSheetResult.Detail.class);
            given(orderSheetAppService.getOrderSheet(orderSheetId, userId))
                    .willReturn(result);
            OrderSheetResponse.Detail response = OrderSheetResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(get("/order-sheets/{sheetId}", orderSheetId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
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
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" +orderSheetId));
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
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/order-sheets/" + orderSheetId));
        }
    }

}