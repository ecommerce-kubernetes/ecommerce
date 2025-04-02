package com.example.order_service.controller;

import com.example.order_service.common.advice.ControllerAdvice;
import com.example.order_service.dto.request.OrderItemRequestDto;
import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderItemResponseDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.service.JwtValidator;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(ControllerAdvice.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @MockitoBean
    JwtValidator jwtValidator;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("주문 생성 테스트")
    void createOrderTest() throws Exception {
        List<OrderItemRequestDto> requestItems = new ArrayList<>();
        requestItems.add(new OrderItemRequestDto(1L, 10));
        requestItems.add(new OrderItemRequestDto(2L, 20));
        requestItems.add(new OrderItemRequestDto(5L, 40));

        OrderRequestDto orderRequestDto = new OrderRequestDto(requestItems, "서울특별시 종로구 세종대로 209");
        String requestBody = mapper.writeValueAsString(orderRequestDto);

        List<OrderItemResponseDto> responseItems = new ArrayList<>();
        responseItems.add(new OrderItemResponseDto(1L, "사과", 10, 2000));
        responseItems.add(new OrderItemResponseDto(2L, "바나나", 20, 3000));
        responseItems.add(new OrderItemResponseDto(5L, "포도", 40, 5000));
        int totalPrice = 0;
        for (OrderItemResponseDto responseItem : responseItems) {
            totalPrice = totalPrice + (responseItem.getPrice() * responseItem.getQuantity());
        }
        OrderResponseDto orderResponseDto = new OrderResponseDto(1L, 1L, responseItems, "서울특별시 종로구 세종대로 209", totalPrice , "PENDING");

        when(jwtValidator.getSub(any(String.class))).thenReturn("1");
        when(orderService.saveOrder(any(Long.class),any(OrderRequestDto.class))).thenReturn(orderResponseDto);

        ResultActions perform = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer testToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));


        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId()))
                .andExpect(jsonPath("$.userId").value(orderResponseDto.getUserId()))
                .andExpect(jsonPath("$.deliveryAddress").value(orderResponseDto.getDeliveryAddress()))
                .andExpect(jsonPath("$.totalPrice").value(orderResponseDto.getTotalPrice()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        for (int i = 0; i < orderResponseDto.getItems().size(); i++) {
            OrderItemResponseDto expectedItem = orderResponseDto.getItems().get(i);
            perform.andExpect(jsonPath("$.items[" + i + "].productId").value(expectedItem.getProductId()))
                    .andExpect(jsonPath("$.items[" + i + "].productName").value(expectedItem.getProductName()))
                    .andExpect(jsonPath("$.items[" + i + "].quantity").value(expectedItem.getQuantity()))
                    .andExpect(jsonPath("$.items[" + i + "].price").value(expectedItem.getPrice()));
        }
    }

    @ParameterizedTest(name = "{1} 필드 => {2}")
    @MethodSource("provideInvalidOrderRequests")
    @DisplayName("주문 생성 테스트 - 입력값 검증 테스트")
    void createOrderTest_InvalidOrderRequestDto(OrderRequestDto orderRequestDto, String expectedField, String expectedMessage) throws Exception {
        String requestBody = mapper.writeValueAsString(orderRequestDto);

        ResultActions perform = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer testToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value(hasItem(expectedField)))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem(expectedMessage)))
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    private static Stream<Arguments> provideInvalidOrderRequests(){
        List<OrderItemRequestDto> requestItems = new ArrayList<>();
        requestItems.add(new OrderItemRequestDto(1L,10));
        requestItems.add(new OrderItemRequestDto(2L, 20));
        requestItems.add(new OrderItemRequestDto(5L, 40));

        List<OrderItemRequestDto> invalidProductIdItems = new ArrayList<>();
        invalidProductIdItems.add(new OrderItemRequestDto(null, 10));

        List<OrderItemRequestDto> invalidQuantityItems = new ArrayList<>();
        invalidQuantityItems.add(new OrderItemRequestDto(1L, 0));
        return Stream.of(
                //items 리스트 사이즈 0
                Arguments.of(
                        new OrderRequestDto(new ArrayList<>(), "서울특별시 종로구 세종대로 209"),
                        "items",
                        "items size must be between 1 and 10"
                ),
                //items 값이 없을때
                Arguments.of(
                        new OrderRequestDto(null, "서울특별시 종로구 세종대로 209"),
                        "items",
                        "Order items is required"
                ),
                //배송지 주소가 없을때
                Arguments.of(
                        new OrderRequestDto(requestItems, ""),
                        "deliveryAddress",
                        "Delivery Address is required"
                ),
                //주문 상품 아이디가 없을때
                Arguments.of(
                        new OrderRequestDto(invalidProductIdItems, "서울특별시 종로구 세종대로 209"),
                        "items[0].productId",
                        "Product Id is required"
                ),
                //주문 상품 갯수가 1 이하일때
                Arguments.of(
                        new OrderRequestDto(invalidQuantityItems, "서울특별시 종로구 세종대로 209"),
                        "items[0].quantity",
                        "Order Quantity must not be less 1"
                )
        );
    }
}