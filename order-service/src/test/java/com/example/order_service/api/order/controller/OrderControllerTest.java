package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Import(TestSecurityConfig.class)
class OrderControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("주문을 생성한다")
    @WithCustomMockUser
    void createOrder() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().build();
        given(orderApplicationService.createOrder(any(CreateOrderDto.class)))
                .willReturn(createCreateOrderResponse(1L));
        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderName").value("상품1"))
                .andExpect(jsonPath("$.finalPaymentAmount").value(2400L))
                .andExpect(jsonPath("$.createdAt").exists());
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
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 주문 상품은 필수이다")
    @WithCustomMockUser
    void createOrderWithEmptyItems() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().items(null).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("주문 상품은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 주문 상품의 productVariantId는 필수이다")
    @WithCustomMockUser
    void createOrderWithoutProductVariantId() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest()
                .items(
                        List.of(CreateOrderItemRequest.builder().productVariantId(null).quantity(1).build())
                ).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("productVariantId는 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 상품 수량은 필수이다")
    @WithCustomMockUser
    void createOrderWithoutQuantity() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest()
                .items(
                        List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(null).build()
                        )
                ).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("수량은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 수량은 1이상이여야 한다")
    @WithCustomMockUser
    void createOrderWithQuantityLessThan1() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest()
                .items(
                        List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(0).build()
                        )
                ).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("수량은 1이상이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 배송지는 필수이다")
    @WithCustomMockUser
    void createOrderWithoutDeliveryAddress() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().deliveryAddress(null).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("배송지는 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 사용할 포인트는 필수이다")
    @WithCustomMockUser
    void createOrderWithoutUseToPoint() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().pointToUse(null).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("사용할 포인트는 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 사용할 포인트는 0이상이여야 한다")
    @WithCustomMockUser
    void createOrderWithUseToPointLessThan0() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().pointToUse(-1L).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("사용할 포인트는 0원 이상이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 예상 결제 금액은 필수입니다")
    @WithCustomMockUser
    void createOrderWithoutExpectedPrice() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().expectedPrice(null).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예상 결제 금액은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("주문 요청시 예상 결제 금액은 0원 이상이여야 한다")
    void createOrderWithExpectedPriceLessThan0() throws Exception {
        //given
        CreateOrderRequest request = createBaseRequest().expectedPrice(-1L).build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예상 결제 금액은 0원 이상이여야 합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("결제 승인시 해당 주문의 정보를 반환한다")
    @WithCustomMockUser
    void confirm() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();
        OrderDetailResponse orderDetailResponse = createOrderDetailResponse();

        given(orderApplicationService.confirmOrder(anyLong(), anyString()))
                .willReturn(orderDetailResponse);
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.orderStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.orderItems").isNotEmpty());
    }

    @Test
    @DisplayName("결제 승인시 주문 ID는 필수이다")
    @WithCustomMockUser
    void confirm_without_no_orderId() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().orderId(null).build();
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("주문 Id는 필수 입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @Test
    @DisplayName("결제 승인시 결제 키는 필수이다")
    @WithCustomMockUser
    void confirm_without_no_paymentKey() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().paymentKey(null).build();
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("결제 키는 필수 입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @Test
    @DisplayName("결제 오류 발생시 에러 응답을 반환한다")
    @WithCustomMockUser
    void confirm_exception_payment() throws Exception {
        //given
        OrderConfirmRequest request = confirmBaseRequest().build();

        willThrow(new PaymentException("결제 오류", PaymentErrorCode.APPROVAL_FAIL))
                .given(orderApplicationService).confirmOrder(anyLong(), anyString());
        //when
        //then
        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("결제 오류"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders/confirm"));
    }

    @Test
    @DisplayName("주문 정보를 조회한다")
    @WithCustomMockUser
    void getOrder() throws Exception {
        //given
        OrderDetailResponse response = createOrderDetailResponse();

        given(orderApplicationService.getOrder(any(UserPrincipal.class),anyLong()))
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

        given(orderApplicationService.getOrders(any(UserPrincipal.class), any(OrderSearchCondition.class)))
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
        verify(orderApplicationService, times(1)).getOrders(any(UserPrincipal.class), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 10, "latest", null, null);
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

        given(orderApplicationService.getOrders(any(UserPrincipal.class), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderApplicationService, times(1)).getOrders(any(UserPrincipal.class), captor.capture());

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

        given(orderApplicationService.getOrders(any(UserPrincipal.class), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderApplicationService, times(1)).getOrders(any(UserPrincipal.class), captor.capture());

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

        given(orderApplicationService.getOrders(any(UserPrincipal.class), any(OrderSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("size", "101"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(orderApplicationService, times(1)).getOrders(any(UserPrincipal.class), captor.capture());

        assertThat(captor.getValue())
                .extracting(OrderSearchCondition::getPage, OrderSearchCondition::getSize, OrderSearchCondition::getSort, OrderSearchCondition::getYear,
                        OrderSearchCondition::getProductName)
                .containsExactly(1, 100, "latest", null, null);
    }

    private OrderDetailResponse createOrderDetailResponse() {
        return OrderDetailResponse.builder()
                .orderId(1L)
                .userId(1L)
                .orderStatus("COMPLETED")
                .orderName("상품1")
                .deliveryAddress("서울시 테헤란로 123")
                .orderPriceResponse(
                        OrderDetailResponse.OrderPriceResponse.builder()
                                .totalOriginPrice(30000L)
                                .totalProductDiscount(3000L)
                                .couponDiscount(1000L)
                                .pointDiscount(1000L)
                                .finalPaymentAmount(25000L)
                                .build()
                )
                .couponResponse(
                        OrderDetailResponse.CouponResponse.builder()
                                .couponId(1L)
                                .couponName("1000원 할인 쿠폰")
                                .couponDiscount(1000L)
                                .build()
                )
                .paymentResponse(
                        OrderDetailResponse.PaymentResponse.builder()
                                .paymentId(1L)
                                .paymentKey("paymentKey")
                                .amount(25000L)
                                .method("CARD")
                                .approvedAt(LocalDateTime.now().toString())
                                .build()
                )
                .orderItems(createOrderItemResponse())
                .build();
    }

    private CreateOrderResponse createCreateOrderResponse(Long orderId){
        return CreateOrderResponse.builder()
                .orderId(orderId)
                .status("PENDING")
                .orderName("상품1")
                .finalPaymentAmount(2400L)
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    private OrderListResponse createOrderListResponse(){
        return OrderListResponse.builder()
                .orderId(1L)
                .userId(1L)
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
                        .thumbNailUrl("http://thumbanil.jpg")
                        .quantity(1)
                        .unitPrice(
                                OrderItemResponse.OrderItemPrice.builder()
                                        .originalPrice(30000L)
                                        .discountAmount(3000L)
                                        .discountRate(10)
                                        .discountedPrice(27000L).build()
                        )
                        .lineTotal(27000L)
                        .options(
                                List.of(OrderItemResponse.OrderItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build())
                        )
                        .build());
    }

    private CreateOrderRequest.CreateOrderRequestBuilder createBaseRequest() {
        return CreateOrderRequest.builder()
                .items(List.of(CreateOrderItemRequest.builder().productVariantId(1L).quantity(1).build()))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L);
    }

    private OrderConfirmRequest.OrderConfirmRequestBuilder confirmBaseRequest() {
        return OrderConfirmRequest.builder()
                .orderId(1L)
                .paymentKey("paymentKey");
    }
}