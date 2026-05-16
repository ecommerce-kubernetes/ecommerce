package com.example.order_service.order.api;

import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.common.dto.PageDto;
import com.example.order_service.common.security.model.UserRole;
import com.example.order_service.order.api.dto.request.*;
import com.example.order_service.order.api.dto.response.OrderResponse;
import com.example.order_service.order.application.OrderAppService;
import com.example.order_service.order.application.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.order.application.dto.result.OrderListResponse;
import com.example.order_service.order.application.dto.result.OrderResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

import static com.example.order_service.api.support.fixture.order.OrderResponseFixture.anOrderDetailResponse;
import static com.example.order_service.api.support.fixture.order.OrderResponseFixture.anOrderListResponse;
import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderAppService orderAppService;
    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("주문을 생성한다")
        @WithCustomMockUser
        void createOrder() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            OrderResult.Create result = fixtureMonkey.giveMeOne(OrderResult.Create.class);
            given(orderAppService.initialOrder(any(OrderCommand.Create.class)))
                    .willReturn(result);
            OrderResponse.Create response = OrderResponse.Create.from(result);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isAccepted())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("주문 요청시 권한은 유저여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
        void createOrderWithAdminPrincipal() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 주문을 생성할 수 없다")
        void createOrder_unAuthorized() throws Exception {
            //given
            OrderRequest.Create request = fixtureMonkey.giveMeOne(OrderRequest.Create.class);
            //when
            //then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("주문 요청시 유효성 검증에 실패하면 400 에러를 반환한다")
        @MethodSource("provideInvalidCreateOrderRequest")
        @WithCustomMockUser
        void createOrder_validation(String description, OrderRequest.Create request, String errorMessage) throws Exception {
            //given
            //when, then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/orders"));
        }

        private static Stream<Arguments> provideInvalidCreateOrderRequest() {
            OrderRequest.Delivery VALID_BASE_DELIVERY = OrderRequest.Delivery.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .baseAddress("서울시 테헤란로 123")
                    .detailAddress("아파트 1234호")
                    .build();
            return Stream.of(
                    Arguments.of(
                            "orderSheet id null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(null)
                                    .deliveryAddress(VALID_BASE_DELIVERY)
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "주문서 ID는 필수 입니다"
                    ),
                    Arguments.of("배송지 정보가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(null)
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "배송지 정보는 필수 입니다"
                    ),
                    Arguments.of(
                            "수령인이 blank",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().receiverName("").build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "수령인 이름은 필수 입니다"
                    ),
                    Arguments.of(
                            "수령인 연락처가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().receiverPhone(null).build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "수령인 연락처는 필수 입니다"
                    ),
                    Arguments.of(
                            "수령인 연락처가 유효하지 않음",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().receiverPhone("123124").build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
                    ),
                    Arguments.of(
                            "우편번호가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().zipCode(null).build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "우편 번호는 필수 입니다"
                    ),
                    Arguments.of(
                            "기본 주소가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().baseAddress(null).build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "기본 주소는 필수 입니다"
                    ),
                    Arguments.of(
                            "상세 주소가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY.toBuilder().detailAddress(null).build())
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "상세 주소는 필수 입니다"
                    ),
                    Arguments.of(
                            "사용 포인트가 null",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY)
                                    .couponId(null)
                                    .pointToUse(null)
                                    .expectedPrice(10000L)
                                    .build(),
                            "사용할 포인트는 필수 입니다"
                    ),
                    Arguments.of(
                            "사용 포인트가 0이하",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY)
                                    .couponId(null)
                                    .pointToUse(-1L)
                                    .expectedPrice(10000L)
                                    .build(),
                            "사용할 포인트는 0 이상이여야 합니다"
                    ),
                    Arguments.of(
                            "예상 결제 금액이 1미만",
                            OrderRequest.Create.builder()
                                    .orderSheetId(1L)
                                    .deliveryAddress(VALID_BASE_DELIVERY)
                                    .couponId(null)
                                    .pointToUse(0L)
                                    .expectedPrice(0L)
                                    .build(),
                            "예상 결제 금액은 1 이상이여야 합니다"
                    )
            );
        }
    }

    @Test
    @DisplayName("결제 승인시 해당 주문의 정보를 반환한다")
    @WithCustomMockUser
    void confirm() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();
        OrderDetailResponse response = anOrderDetailResponse().build();

        given(orderAppService.confirmOrderPayment(anyString(), anyLong(), anyString(), anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("결제 승인시 권한은 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void confirm_Admin_role() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 결제 승인을 요청할 수 없다")
    void confirm_unAuthorized() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("결제 승인 요청시 유효성 검증에 실패하면 400 에러를 반환한다")
    @MethodSource("provideInvalidConfirmRequest")
    @WithCustomMockUser
    void confirm_validation(String description, OrderConfirmRequest request, String errorMessage) throws Exception {
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @Test
    @DisplayName("주문 정보를 조회한다")
    @WithCustomMockUser
    void getOrder() throws Exception {
        //given
        OrderDetailResponse response = anOrderDetailResponse().build();
        given(orderAppService.getOrder(anyLong(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("주문 정보를 조회할때는 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void getOrder_Admin_role() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 주문 정보를 조회할 수 없다")
    void getOrder_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/1"));
    }

    @Test
    @DisplayName("주문 목록 조회")
    @WithCustomMockUser
    void getOrders() throws Exception {
        //given
        OrderListResponse orderListResponse = anOrderListResponse().build();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();

        paramMap.add("page", "1");
        paramMap.add("size", "10");
        paramMap.add("sort", "latest");

        given(orderAppService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(get("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .params(paramMap))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderAppService, times(1)).getOrders(anyLong(), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 10, "latest", null, null);
    }

    @Test
    @DisplayName("주문 목록 조회시 권한은 유저여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void getOrders_Admin_role() throws Exception {
        //given
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("page", "1");
        paramMap.add("size", "10");
        paramMap.add("sort", "latest");
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(paramMap))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 주문 목록을 조회할 수 없다")
    void getOrders_unAuthorized() throws Exception {
        //given
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("page", "1");
        paramMap.add("size", "10");
        paramMap.add("sort", "latest");
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(paramMap))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 목록 조회시 page, size, sort 파라미터 값이 없으면 기본값으로 요청된다")
    @WithCustomMockUser
    void getOrders_default() throws Exception {
        //given
        OrderListResponse orderListResponse = anOrderListResponse().build();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderAppService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderAppService, times(1)).getOrders(anyLong(), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 20, "latest", null, null);
    }
    
    @Test
    @DisplayName("주문 목록 조회시 page 쿼리 파라미터가 0 이하면 page는 1로 요청된다")
    @WithCustomMockUser
    void getOrder_page_less_than_0() throws Exception {
        //given
        OrderListResponse orderListResponse = anOrderListResponse().build();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderAppService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderAppService, times(1)).getOrders(anyLong(), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 20, "latest", null, null);
    }
    
    @Test
    @DisplayName("주문 목록 조회시 size가 100 보다 크면 size는 100으로 요청된다")
    @WithCustomMockUser
    void getOrder_size_greater_than_100() throws Exception {
        //given
        OrderListResponse orderListResponse = anOrderListResponse().build();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderAppService.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", "101"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderAppService, times(1)).getOrders(anyLong(), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 100, "latest", null, null);
    }

    private static Stream<Arguments> provideInvalidConfirmRequest(){
        return Stream.of(
                Arguments.of("주문 ID null", confirmBaseRequest().orderNo(null).build(), "주문 번호는 필수 입니다"),
                Arguments.of("결제 키 null", confirmBaseRequest().paymentKey(null).build(), "결제 키는 필수 입니다"),
                Arguments.of("결제 가격 null", confirmBaseRequest().amount(null).build(), "결제 가격은 필수 입니다")
        );
    }

    private static CreateOrderRequest.CreateOrderRequestBuilder createBaseRequest() {
        return CreateOrderRequest.builder()
                .items(List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(1).build()))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L);
    }

    private static OrderConfirmRequest.OrderConfirmRequestBuilder confirmBaseRequest() {
        return OrderConfirmRequest.builder()
                .orderNo(ORDER_NO)
                .paymentKey("paymentKey")
                .amount(10000L);
    }
}