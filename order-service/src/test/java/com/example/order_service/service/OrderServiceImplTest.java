package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.OrderItemRequestDto;
import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderItemResponseDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrderItemsRepository;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
class OrderServiceImplTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    OrderItemsRepository orderItemsRepository;

    @MockitoBean
    ProductClientService productClientService;

    @MockitoBean
    KafkaProducer kafkaProducer;

    @Test
    @Transactional
    void saveOrderTest(){
        Long userId = 1L;
        List<OrderItemRequestDto> orderItemRequestDtoList = new ArrayList<>();
        orderItemRequestDtoList.add(new OrderItemRequestDto(1L, 10));
        orderItemRequestDtoList.add(new OrderItemRequestDto(2L, 20));
        OrderRequestDto orderRequestDto = new OrderRequestDto(orderItemRequestDtoList,"delivery Address");

        when(productClientService.fetchProductBatch(any(ProductRequestIdsDto.class)))
                .thenAnswer(invocation -> {
                    ProductRequestIdsDto dto = invocation.getArgument(0);
                    // dto.getIds()에 담긴 각 productId에 대해 ProductResponseDto 생성.
                    return dto.getIds().stream()
                            .map(productId -> new ProductResponseDto(
                                    productId,
                                    "name" + productId,
                                    "description" + productId,
                                    1000,
                                    10,
                                    1L,
                                    "http://" + productId + "/image.jpg"))
                            .toList();
                });
        OrderResponseDto orderResponseDto = orderService.saveOrder(userId, orderRequestDto);

        Orders orders = ordersRepository.findById(orderResponseDto.getId())
                .orElseThrow();

        //주문 검증
        assertThat(orders.getId()).isEqualTo(orderResponseDto.getId());
        assertThat(orders.getUserId()).isEqualTo(orderResponseDto.getUserId());
        assertThat(orders.getStatus()).isEqualTo(orderResponseDto.getStatus());
        assertThat(orders.getDeliveryAddress()).isEqualTo(orderResponseDto.getDeliveryAddress());

        List<OrderItems> orderItems = orderItemsRepository.findByOrderId(orderResponseDto.getId());
        List<OrderItemResponseDto> items = orderResponseDto.getItems();

        assertThat(items).hasSize(orderItems.size());

        //주문 아이템 검증
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItems dbItem = orderItems.get(i);
            OrderItemResponseDto dtoItem = items.get(i);

            assertThat(dbItem.getProductId()).isEqualTo(dtoItem.getProductId());
            assertThat(dbItem.getProductName()).isEqualTo(dtoItem.getProductName());
            assertThat(dbItem.getPrice()).isEqualTo(dtoItem.getPrice());
            assertThat(dbItem.getQuantity()).isEqualTo(dtoItem.getQuantity());
            assertThat(dbItem.getMainImgUrl()).isEqualTo(dtoItem.getMainImgUrl());
        }

        int totalPrice = orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        //주문 가격 검증
        assertThat(orderResponseDto.getTotalPrice()).isEqualTo(totalPrice);

        //KafkaProducer 호출 검증
        verify(kafkaProducer).sendMessage(anyString(), any());
    }

    @Test
    void saveOrderTest_ProductClientServiceNotFound(){
        Long userId = 1L;
        List<OrderItemRequestDto> orderItemRequestDtoList = new ArrayList<>();
        orderItemRequestDtoList.add(new OrderItemRequestDto(1L, 10));
        orderItemRequestDtoList.add(new OrderItemRequestDto(2L, 20));
        OrderRequestDto orderRequestDto = new OrderRequestDto(orderItemRequestDtoList,"delivery Address");

        doThrow(new NotFoundException("Not Found Product")).when(productClientService).fetchProductBatch(any(ProductRequestIdsDto.class));

        assertThatThrownBy(() -> orderService.saveOrder(userId,orderRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Product");
    }

    @ParameterizedTest
    @MethodSource("orderTestParameters")
    @Transactional
    void getOrderListTest(String keyword, int expectOrderItemCount){
        prepareTestOrders();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createAt");

        PageDto<OrderResponseDto> orderServiceResult = orderService.getOrderList(pageable, 1L, null, keyword);

        assertThat(orderServiceResult.getTotalElement()).isEqualTo(expectOrderItemCount);

        orderServiceResult.getContent().forEach(order -> assertThat(order.getItems())
                .extracting(OrderItemResponseDto::getProductName)
                .anyMatch(product -> product.contains(keyword)));
    }

    private void prepareTestOrders(){
        Orders order1 = new Orders(1L, 10000, "PENDING", "서울특별시 종로구");
        Orders order2 = new Orders(1L, 10000, "PENDING", "서울특별시 종로구");
        Orders order3 = new Orders(1L, 20000, "PENDING", "서울특별시 종로구");

        ordersRepository.saveAll(List.of(order1, order2, order3));

        new OrderItems(order1, 1L, "사과", 1000, 10,"http://apple.jpg");

        new OrderItems(order2, 1L, "사과", 1000, 5,"http://apple.jpg");
        new OrderItems(order2, 2L, "바나나", 1000, 5,"http://banana.jpg");

        new OrderItems(order3, 1L, "사과", 1000, 5,"http://apple.jpg");
        new OrderItems(order3, 2L, "바나나", 1000, 5,"http://banana.jpg");
        new OrderItems(order3, 3L, "포도", 1000, 10,"http://grape.jpg");
    }

    private static Stream<Arguments> orderTestParameters(){
        return Stream.of(
                Arguments.of("", 3),
                Arguments.of("사과",3),
                Arguments.of("바나나",2),
                Arguments.of("포도",1)
        );
    }
}