package com.example.order_service.service.order;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.api.order.domain.model.OrderItems;
import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.order.domain.repository.OrdersRepository;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
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

@SpringBootTest
class OrderDomainServiceTest {

    @Autowired
    OrderDomainService orderDomainService;
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
    CartProductClientService cartProductClientService;
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
        List<OrderItems> orderItems = List.of(new OrderItems(1L, 1L, "상품1", List.of(CartProductResponse.ItemOption.builder()
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
        orderDomainService.completeOrder(saveOrder.getId());
        em.flush(); em.clear();

        Orders orders = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(orders.getStatus()).isEqualTo("COMPLETE");
    }

    @Test
    @DisplayName("주문 상태 변경 - CANCELLED")
    @Transactional
    void cancelOrderTest(){
        orderDomainService.cancelOrder(saveOrder.getId());
        em.flush(); em.clear();

        Orders orders = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(orders.getStatus()).isEqualTo("CANCEL");
    }
}