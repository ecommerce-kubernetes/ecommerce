package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderItemResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
class OrderControllerTest extends ControllerTestSupport {

    private static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("주문을 생성한다")
    @WithCustomMockUser
    void createOrder() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().build();
        CreateOrderResponse response = createCreateOrderResponse();
        given(orderFacade.initialOrder(any(CreateOrderCommand.class)))
                .willReturn(response);
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
        CreateOrderRequest request = createBaseRequest().build();
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
        CreateOrderRequest request = createBaseRequest().build();
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
    void createOrder_validation(String description, CreateOrderRequest request, String errorMessage) throws Exception {
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

    @Test
    @DisplayName("결제 승인시 해당 주문의 정보를 반환한다")
    @WithCustomMockUser
    void confirm() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();
        OrderDetailResponse response = createOrderDetailResponse();

        given(orderFacade.confirmOrderPayment(anyString(), anyLong(), anyString(), anyLong()))
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
        OrderDetailResponse response = createOrderDetailResponse();

        given(orderFacade.getOrder(anyLong(), anyString()))
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
        OrderListResponse orderListResponse = createOrderListResponse();
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

        given(orderFacade.getOrders(anyLong(), any(OrderSearchCondition.class)))
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
        verify(orderFacade, times(1)).getOrders(anyLong(), captor.capture());

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
        OrderListResponse orderListResponse = createOrderListResponse();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderFacade.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderFacade, times(1)).getOrders(anyLong(), captor.capture());

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
        OrderListResponse orderListResponse = createOrderListResponse();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderFacade.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderFacade, times(1)).getOrders(anyLong(), captor.capture());

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
        OrderListResponse orderListResponse = createOrderListResponse();
        PageDto<OrderListResponse> response = PageDto.<OrderListResponse>builder().content(List.of(orderListResponse))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();

        given(orderFacade.getOrders(anyLong(), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", "101"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderFacade, times(1)).getOrders(anyLong(), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 100, "latest", null, null);
    }

    private static Stream<Arguments> provideInvalidCreateOrderRequest() {
        return Stream.of(
                Arguments.of("주문 상품 null", createBaseRequest().items(null).build(), "주문 상품은 필수입니다"),
                Arguments.of("상품 VariantId null",
                        createBaseRequest().items(List.of(CreateOrderItemRequest.builder().productVariantId(null).quantity(3).build())).build(),
                        "productVariantId는 필수입니다"
                ),
                Arguments.of("주문 수량 null",
                        createBaseRequest().items(List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(null).build())).build(),
                        "수량은 필수입니다"),
                Arguments.of("주문 수량 1미만",
                        createBaseRequest().items(List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(0).build())).build(),
                        "수량은 1이상이여야 합니다"),
                Arguments.of("배송지 null",
                        createBaseRequest().deliveryAddress(null).build(),
                        "배송지는 필수입니다"),
                Arguments.of("사용 포인트 null",
                        createBaseRequest().pointToUse(null).build(),
                        "사용할 포인트는 필수입니다"),
                Arguments.of("사용 포인트 0미만",
                        createBaseRequest().pointToUse(-1L).build(),
                        "사용할 포인트는 0 이상이여야 합니다"),
                Arguments.of("예상 결제 금액 null",
                        createBaseRequest().expectedPrice(null).build(),
                        "예상 결제 금액은 필수입니다"),
                Arguments.of("예상 결제 금액 1 미만",
                        createBaseRequest().expectedPrice(0L).build(),
                        "예상 결제 금액은 1 이상이여야 합니다")
        );
    }

    private static Stream<Arguments> provideInvalidConfirmRequest(){
        return Stream.of(
                Arguments.of("주문 ID null", confirmBaseRequest().orderNo(null).build(), "주문 번호는 필수 입니다"),
                Arguments.of("결제 키 null", confirmBaseRequest().paymentKey(null).build(), "결제 키는 필수 입니다"),
                Arguments.of("결제 가격 null", confirmBaseRequest().amount(null).build(), "결제 가격은 필수 입니다")
        );
    }

    private OrderDetailResponse createOrderDetailResponse() {
//        return OrderDetailResponse.builder()
//                .orderNo(ORDER_NO)
//                .userId(1L)
//                .orderStatus("COMPLETED")
//                .orderName("상품1")
//                .deliveryAddress("서울시 테헤란로 123")
//                .orderPriceResponse(
//                        OrderDetailResponse.OrderPriceResponse.builder()
//                                .totalOriginPrice(30000L)
//                                .totalProductDiscount(3000L)
//                                .couponDiscount(1000L)
//                                .pointDiscount(1000L)
//                                .finalPaymentAmount(25000L)
//                                .build()
//                )
//                .couponResponse(
//                        OrderDetailResponse.CouponResponse.builder()
//                                .couponId(1L)
//                                .couponName("1000원 할인 쿠폰")
//                                .couponDiscount(1000L)
//                                .build()
//                )
//                .paymentResponse(
//                        OrderDetailResponse.PaymentResponse.builder()
//                                .paymentId(1L)
//                                .paymentKey("paymentKey")
//                                .amount(25000L)
//                                .method("CARD")
//                                .approvedAt(LocalDateTime.now().toString())
//                                .build()
//                )
//                .orderItems(createOrderItemResponse())
//                .build();
        return null;
    }

    private CreateOrderResponse createCreateOrderResponse(){
        return CreateOrderResponse.builder()
                .orderNo(ORDER_NO)
                .status("PENDING")
                .orderName("상품1")
                .finalPaymentAmount(2400L)
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    private OrderListResponse createOrderListResponse(){
        return OrderListResponse.builder()
                .orderNo(ORDER_NO)
                .orderStatus("COMPLETED")
                .orderItems(createOrderItemResponse())
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    private List<OrderItemResponse> createOrderItemResponse() {
        return List.of(
                OrderItemResponse.builder()
                        .productId(1L)
                        .productVariantId(1L)
                        .productName("상품1")
                        .thumbnailUrl("http://thumbanil.jpg")
                        .quantity(1)
                        .unitPrice(
                                OrderItemResponse.OrderItemPriceResponse.builder()
                                        .originalPrice(30000L)
                                        .discountAmount(3000L)
                                        .discountRate(10)
                                        .discountedPrice(27000L).build()
                        )
                        .lineTotal(27000L)
                        .options(
                                List.of(OrderItemResponse.OrderItemOptionResponse.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build())
                        )
                        .build());
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