package com.example.order_service.controller;

import com.example.order_service.common.advice.ControllerAdvice;
import com.example.order_service.dto.request.OrderItemRequestDto;
import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderItemResponseDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.exception.NotFoundException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        OrderResponseDto orderResponseDto = new OrderResponseDto(1L, 1L, responseItems, "서울특별시 종로구 세종대로 209", totalPrice , "PENDING", LocalDateTime.now());

        when(orderService.saveOrder(any(Long.class),any(OrderRequestDto.class))).thenReturn(orderResponseDto);

        ResultActions perform = mockMvc.perform(post("/orders")
                .header("user-id", "1")
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
                .header("user-id", "1")
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

    @Test
    @DisplayName("주문 생성 테스트 - 상품이 존재하지 않을때")
    void createOrderTest_NotFoundProduct() throws Exception {
        List<OrderItemRequestDto> requestItems = new ArrayList<>();
        requestItems.add(new OrderItemRequestDto(1L, 10));
        requestItems.add(new OrderItemRequestDto(2L, 20));
        requestItems.add(new OrderItemRequestDto(5L, 40));

        OrderRequestDto orderRequestDto = new OrderRequestDto(requestItems, "서울특별시 종로구 세종대로 209");
        String requestBody = mapper.writeValueAsString(orderRequestDto);
        doThrow(new NotFoundException("Not Found Product")).when(orderService).saveOrder(anyLong(), any(OrderRequestDto.class));

        ResultActions perform = mockMvc.perform(post("/orders")
                .header("user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Product"))
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @ParameterizedTest
    @MethodSource("orderDataProvider")
    @DisplayName("주문 목록 조회 테스트")
    void getAllOrdersTest(PageDto<OrderResponseDto> pageDto) throws Exception {
        when(orderService.getOrderList(any(Pageable.class), anyLong(), nullable(Integer.class), nullable(String.class)))
                .thenReturn(pageDto);

        ResultActions perform = mockMvc.perform(get("/orders/1"));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(pageDto.getCurrentPage()))
                .andExpect(jsonPath("$.totalPage").value(pageDto.getTotalPage()))
                .andExpect(jsonPath("$.pageSize").value(pageDto.getPageSize()))
                .andExpect(jsonPath("$.totalElement").value(pageDto.getTotalElement()));

        List<OrderResponseDto> orderResponseDtos = pageDto.getContent();
        for(int i=0; i< orderResponseDtos.size(); i++){
            OrderResponseDto order = orderResponseDtos.get(i);

            perform
                    .andExpect(jsonPath("$.content[" + i + "].id").value(order.getId()))
                    .andExpect(jsonPath("$.content[" + i + "].userId").value(order.getUserId()))
                    .andExpect(jsonPath("$.content[" + i + "].deliveryAddress").value(order.getDeliveryAddress()))
                    .andExpect(jsonPath("$.content[" + i + "].totalPrice").value(order.getTotalPrice()))
                    .andExpect(jsonPath("$.content[" + i + "].status").value(order.getStatus()));

            List<OrderItemResponseDto> items = order.getItems();
            for(int j=0; j<items.size(); j++){
                OrderItemResponseDto item = items.get(j);
                perform
                        .andExpect(jsonPath("$.content[" + i + "].items[" + j + "].productId").value(item.getProductId()))
                        .andExpect(jsonPath("$.content[" + i + "].items[" + j + "].productName").value(item.getProductName()))
                        .andExpect(jsonPath("$.content[" + i + "].items[" + j + "].price").value(item.getPrice()))
                        .andExpect(jsonPath("$.content[" + i + "].items[" + j + "].quantity").value(item.getQuantity()));
            }
        }
    }

    @Test
    @DisplayName("주문 목록 조회 테스트 - 주문을 찾을 수 없을때")
    void getAllOrdersByUserIdTest_NotFoundOrder() throws Exception {
        doThrow(new NotFoundException("Not Found Order")).when(orderService).getOrderList(any(Pageable.class), anyLong(), nullable(Integer.class), nullable(String.class));

        ResultActions perform = mockMvc.perform(get("/orders/1"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Order"))
                .andExpect(jsonPath("$.path").value("/orders/1"));

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

    private static Stream<Arguments> orderDataProvider(){
        List<Arguments> arguments = new ArrayList<>();

        //주문 1개 주문 아이템도 1개
        List<OrderItemResponseDto> orderItemResponseDtoListByOneOrderItem = new ArrayList<>();
        orderItemResponseDtoListByOneOrderItem.add(new OrderItemResponseDto(1L, "사과", 10, 1000));
        List<OrderResponseDto> orderResponseDtoListByOneOrder = new ArrayList<>();
        orderResponseDtoListByOneOrder.add(new OrderResponseDto(1L, 1L, orderItemResponseDtoListByOneOrderItem,
                "delivery Address", 10000, "PENDING", LocalDateTime.now()));
        PageDto<OrderResponseDto> case1PageDto = new PageDto<>(orderResponseDtoListByOneOrder, 0, 1, 10, 1);
        arguments.add(Arguments.of(case1PageDto));

        List<OrderItemResponseDto> orderItemResponseDtoListByManyOrderItem = new ArrayList<>();
        orderItemResponseDtoListByManyOrderItem.add(new OrderItemResponseDto(2L, "바나나", 5, 200));
        orderItemResponseDtoListByManyOrderItem.add(new OrderItemResponseDto(3L, "포도", 7, 150));
        List<OrderResponseDto> orderResponseDtos = new ArrayList<>();
        orderResponseDtos.add(new OrderResponseDto(2L, 1L, orderItemResponseDtoListByManyOrderItem, "delivery Address", 5000, "DELIVERED", LocalDateTime.now()));
        PageDto<OrderResponseDto> case2PageDto = new PageDto<>(orderResponseDtos, 0, 1, 10, 1);
        arguments.add(Arguments.of(case2PageDto));

        return arguments.stream();


    }
}