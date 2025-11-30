package com.example.order_service.controller.order;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.config.TestConfig;
import com.example.order_service.controller.ControllerTestSupport;
import com.example.order_service.controller.security.WithCustomMockUser;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.service.SseConnectionService;
import com.example.order_service.service.dto.CreateOrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class OrderControllerTest extends ControllerTestSupport {

    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    SseConnectionService sseConnectionService;

    @Test
    @DisplayName("주문을 생성한다")
    @WithCustomMockUser
    void createOrder() throws Exception {
        //given
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(2)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();

        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(1L)
                .status("PENDING")
                .message("주문이 접수되었습니다")
                .createAt(LocalDateTime.now())
                .subscribeUrl("http://subscribe.com")
                .build();

        given(orderService.saveOrder(any(CreateOrderDto.class)))
                .willReturn(response);

        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("주문이 접수되었습니다"))
                .andExpect(jsonPath("$.createAt").exists())
                .andExpect(jsonPath("$.subscribeUrl").value("http://subscribe.com"));
    }

    @Test
    @DisplayName("주문 요청시 주문 상품은 필수이다")
    @WithCustomMockUser
    void createOrderWithEmptyItems() throws Exception {
        //given
        OrderRequest orderRequest = OrderRequest.builder()
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();

        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .quantity(3)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(0)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .couponId(1L)
                .pointToUse(300L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .deliveryAddress("서울시 테헤란로 123")
                .couponId(1L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .couponId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .pointToUse(-1L)
                .expectedPrice(2400L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
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
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .items(List.of(orderItemRequest))
                .couponId(1L)
                .deliveryAddress("서울시 테헤란로 123")
                .pointToUse(0L)
                .build();
        //when
        //then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예상 결제 금액은 필수입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/orders"));
    }

}