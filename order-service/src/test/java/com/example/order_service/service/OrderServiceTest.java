package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
        saveOrder = new Orders(1L, "PENDING", "서울시 테헤란로 123");
        List<OrderItems> orderItems = List.of(new OrderItems(1L, 3));
        saveOrder.addOrderItems(orderItems);
        ordersRepository.save(saveOrder);
    }

    @Test
    @DisplayName("주문 생성 테스트")
    @Transactional
    void saveOrderTest_integration(){
        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                2500,
                200);

        CreateOrderResponse response = orderService.saveOrder(1L, orderRequest);

        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getSubscribeUrl()).isNotNull();

        Orders findOrder = ordersRepository.findById(response.getOrderId()).get();

        assertThat(findOrder)
                .extracting(Orders::getUserId, Orders::getStatus, Orders::getDeliveryAddress)
                .containsExactlyInAnyOrder(
                        1L, "PENDING", "서울시 테헤란로 123"
                );

        assertThat(findOrder.getOrderItems()).hasSize(1);
        assertThat(findOrder.getOrderItems())
                .extracting(OrderItems::getProductVariantId, OrderItems::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L,3)
                );

        List<PendingOrderCreatedEvent> events = testListener.getEvents();
        assertThat(events).isNotEmpty();
        PendingOrderCreatedEvent ev = events.get(0);
        assertThat(ev.getOrderId()).isEqualTo(response.getOrderId());
        assertThat(ev.getOrderRequest().getDeliveryAddress()).isEqualTo(orderRequest.getDeliveryAddress());
    }

    @Test
    @DisplayName("주문 처리 테스트")
    @Transactional
    void finalizedOrderTest(){
        ProductStockDeductedEvent prodEvent = new ProductStockDeductedEvent(saveOrder.getId(), List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg",
                new PriceInfo(3000, 10, 300, 2700), 3, List.of(new ItemOption("색상", "RED")))));
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(saveOrder.getId(), 1L, true, 100, 7700, 7800);
        CouponUsedSuccessEvent couponEvent = new CouponUsedSuccessEvent(saveOrder.getId(), 1L, DiscountType.AMOUNT, 300, 100, 10000);
        Map<Object, Object> sagaStatus = Map.of("product", prodEvent, "user", userEvent, "coupon", couponEvent, "orderId", 1L);
        orderService.finalizeOrder(sagaStatus);
        em.flush(); em.clear();

        Orders findOrder = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(findOrder.getStatus()).isEqualTo("COMPLETE");
    }

    @Test
    @DisplayName("주문 처리 테스트")
    @Transactional
    void finalizedOrderTest_fail(){
        ProductStockDeductedEvent prodEvent = new ProductStockDeductedEvent(saveOrder.getId(), List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg",
                new PriceInfo(3000, 10, 300, 2700), 3, List.of(new ItemOption("색상", "RED")))));
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(saveOrder.getId(), 1L, true, 100, 7700, 7800);
        CouponUsedSuccessEvent couponEvent = new CouponUsedSuccessEvent(saveOrder.getId(), 1L, DiscountType.AMOUNT, 1000, 100, 10000);
        Map<Object, Object> sagaStatus = Map.of("product", prodEvent, "user", userEvent, "coupon", couponEvent, "orderId", 1L);
        orderService.finalizeOrder(sagaStatus);
        em.flush(); em.clear();

        Orders findOrder = ordersRepository.findById(saveOrder.getId()).get();

        assertThat(findOrder.getStatus()).isEqualTo("CANCEL");
    }
}