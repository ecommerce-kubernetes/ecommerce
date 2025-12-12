package com.example.order_service.api.order.controller;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
                        .content(objectMapper.writeValueAsString(createOrderRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예상 결제 금액은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

}