package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
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
    }

    @Test
    void saveOrderTest_ProductClientServiceNotFound(){

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