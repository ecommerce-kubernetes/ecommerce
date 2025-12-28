package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderItemResponse;
import com.example.order_service.api.order.application.dto.result.OrderResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.controller.dto.request.OrderConfirmRequest;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({TestConfig.class, TestSecurityConfig.class})
class OrderControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("주문을 생성한다")
    @WithCustomMockUser
    void createOrder() throws Exception {
        //given
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();

        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(1L)
                .status("PENDING")
                .orderName("상품1 외 1건")
                .createAt(LocalDateTime.now())
                .finalPaymentAmount(2400L)
                .build();

        given(orderApplicationService.createOrder(any(CreateOrderDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderName").value("상품1 외 1건"))
                .andExpect(jsonPath("$.finalPaymentAmount").value(2400L))
                .andExpect(jsonPath("$.createAt").exists());
    }

    @Test
    @DisplayName("주문 요청시 권한은 유저여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
    void createOrderWithAdminPrincipal() throws Exception {
        //given
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();

        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
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
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();

        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .quantity(3)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(0)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .couponId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .pointToUse(-1L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest))
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
        CreateOrderItemRequest createOrderItemRequest = CreateOrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest))
                .couponId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .pointToUse(0L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예상 결제 금액은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    @DisplayName("결제 승인시 해당 주문의 정보를 반환한다")
    @WithCustomMockUser
    void confirm() throws Exception {
        //given
        Long orderId = 1L;
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderId(orderId)
                .paymentKey("paymentKey")
                .build();
        OrderResponse orderResponse = createOrderResponse(orderId);

        given(orderApplicationService.confirmOrder(anyLong(), anyString()))
                .willReturn(orderResponse);
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
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .paymentKey("paymentKey")
                .build();
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
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderId(1L)
                .build();
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
        OrderConfirmRequest request = OrderConfirmRequest.builder()
                .orderId(1L)
                .paymentKey("paymentKey")
                .build();

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
        OrderResponse orderResponse = createOrderResponse(1L);

        given(orderApplicationService.getOrder(any(UserPrincipal.class),anyLong()))
                .willReturn(orderResponse);
        //when
        //then
        mockMvc.perform(get("/orders/{orderId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.orderName").value("상품1"))
                .andExpect(jsonPath("$.deliveryAddress").value("서울시 테헤란로 123"))

                .andExpect(jsonPath("$.paymentResponse.totalOriginPrice").value(30000L))
                .andExpect(jsonPath("$.paymentResponse.totalProductDiscount").value(3000L))
                .andExpect(jsonPath("$.paymentResponse.couponDiscount").value(1000L))
                .andExpect(jsonPath("$.paymentResponse.pointDiscount").value(1000L))
                .andExpect(jsonPath("$.paymentResponse.finalPaymentAmount").value(25000L))

                .andExpect(jsonPath("$.couponResponse.couponId").value(1L))
                .andExpect(jsonPath("$.couponResponse.couponName").value("1000원 할인 쿠폰"))
                .andExpect(jsonPath("$.couponResponse.couponDiscount").value(1000L))

                .andExpect(jsonPath("$.orderItems[0].productId").value(1L))
                .andExpect(jsonPath("$.orderItems[0].productVariantId").value(1L))
                .andExpect(jsonPath("$.orderItems[0].productName").value("상품1"))
                .andExpect(jsonPath("$.orderItems[0].thumbNailUrl").value("http://thumbanil.jpg"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(1))
                .andExpect(jsonPath("$.orderItems[0].unitPrice.originalPrice").value(30000L))
                .andExpect(jsonPath("$.orderItems[0].unitPrice.discountAmount").value(3000L))
                .andExpect(jsonPath("$.orderItems[0].unitPrice.discountRate").value(10))
                .andExpect(jsonPath("$.orderItems[0].unitPrice.discountedPrice").value(27000L))
                .andExpect(jsonPath("$.orderItems[0].lineTotal").value(27000L))
                .andExpect(jsonPath("$.orderItems[0].options[0].optionTypeName").value("사이즈"))
                .andExpect(jsonPath("$.orderItems[0].options[0].optionValueName").value("XL"));

    }

    private OrderResponse createOrderResponse(Long orderId) {
        return OrderResponse.builder()
                .orderId(orderId)
                .userId(1L)
                .orderStatus("COMPLETED")
                .orderName("상품1")
                .deliveryAddress("서울시 테헤란로 123")
                .paymentResponse(
                        OrderResponse.PaymentResponse.builder()
                                .totalOriginPrice(30000L)
                                .totalProductDiscount(3000L)
                                .couponDiscount(1000L)
                                .pointDiscount(1000L)
                                .finalPaymentAmount(25000L)
                                .build()
                )
                .couponResponse(
                        OrderResponse.CouponResponse.builder()
                                .couponId(1L)
                                .couponName("1000원 할인 쿠폰")
                                .couponDiscount(1000L)
                                .build()
                )
                .orderItems(
                        List.of(
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
                                        .build()
                        )
                ).build();

    }
}