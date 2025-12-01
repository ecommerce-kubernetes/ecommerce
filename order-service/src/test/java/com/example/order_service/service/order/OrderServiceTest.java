package com.example.order_service.service.order;

import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrdersRepository ordersRepository;
    @MockitoBean
    ApplicationEventPublisher applicationEventPublisher;
    @MockitoBean
    PendingOrderTimeoutScheduler pendingOrderTimeoutScheduler;
    @Autowired
    EntityManager em;
    @Autowired
    TestPendingOrderListener testListener; // 테스트 전용 리스너
    @MockitoBean
    ProductClientService productClientService;
    @MockitoBean
    UserClientService userClientService;
    @MockitoBean
    CouponClientService couponClientService;

    private Orders saveOrder;
    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestPendingOrderListener testPendingOrderListener() {
            return new TestPendingOrderListener();
        }
    }

    // 테스트 전용 리스너: 발행된 이벤트를 수집
    public static class TestPendingOrderListener implements ApplicationListener<PendingOrderCreatedEvent> {
        private final List<PendingOrderCreatedEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void onApplicationEvent(PendingOrderCreatedEvent event) {
            events.add(event);
        }

        public List<PendingOrderCreatedEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    @BeforeEach
    void setUp() {
        testListener.clear();
        saveOrder = new Orders(1L, 1L, "PENDING", "서울시 테헤란로 123", 3000L, 300L,
                1000L, 700L, 1000L);
        List<OrderItems> orderItems = List.of(new OrderItems(1L, 1L, "상품1", List.of(ItemOptionResponse.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build()),
                1000L, 10, 900L, 2700L, 3, "http://test.jpg"));
        saveOrder.addOrderItems(orderItems);
        ordersRepository.save(saveOrder);
    }

    @Test
    @DisplayName("주문 상태 변경 - COMPLETE")
    @Transactional
    void completeOrderTest(){
        orderService.completeOrder(saveOrder.getId());
        em.flush(); em.clear();

        Orders orders = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(orders.getStatus()).isEqualTo("COMPLETE");
    }

    @Test
    @DisplayName("주문 상태 변경 - CANCELLED")
    @Transactional
    void cancelOrderTest(){
        orderService.cancelOrder(saveOrder.getId());
        em.flush(); em.clear();

        Orders orders = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(orders.getStatus()).isEqualTo("CANCEL");
    }
}