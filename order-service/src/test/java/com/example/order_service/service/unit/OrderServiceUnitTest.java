package com.example.order_service.service.unit;

import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.kafka.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
    @InjectMocks
    OrderService orderService;
    @Mock
    OrdersRepository ordersRepository;
    @Mock
    KafkaProducer kafkaProducer;

    @Test
    @DisplayName("주문 생성 테스트")
    void saveOrderTest(){
        Orders pending = new Orders(1L, "PENDING", "서울시 테헤란로 123");
        ReflectionTestUtils.setField(pending, "id", 1L);
        pending.addOrderItems(List.of(new OrderItems(1L, 2)));
        when(ordersRepository.save(any(Orders.class))).thenReturn(pending);
        List<OrderItemRequest> orderItems = List.of(new OrderItemRequest(1L, 2));
        OrderRequest request = new OrderRequest(orderItems, "서울시 테헤란로 123", 1L, 3000, 500);
        CreateOrderResponse response = orderService.saveOrder(1L, request);

        assertThat(response)
                .extracting(CreateOrderResponse::getOrderId, CreateOrderResponse::getSubscribeUrl)
                .containsExactlyInAnyOrder(
                        1L, "http://test.com/" + 1L + "/subscribe"
                );
    }
}
