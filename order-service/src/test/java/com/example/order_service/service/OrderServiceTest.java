package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.service.client.dto.ProductPrice;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
        saveOrder = new Orders(1L, "PENDING", "서울시 테헤란로 123");
        List<OrderItems> orderItems = List.of(new OrderItems(1L, 3));
        saveOrder.addOrderItems(orderItems);
        ordersRepository.save(saveOrder);
    }

    @Test
    @DisplayName("주문 생성 테스트")
    @Transactional
    void saveOrderTest_integration(){
        //상품 서비스 모킹
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(
                        List.of(new ProductResponse(1L, 1L, "상품1",
                                new ProductPrice(3000, 10, 300, 2700),
                                "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED"))))
                );
        //유저 서비스 모킹
        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenReturn(new UserBalanceResponse(1L, 10000L, 3000L));
        //쿠폰 서비스 모킹
        when(couponClientService.fetchCouponByUserCouponId(anyLong()))
                .thenReturn(new CouponResponse(1L, "AMOUNT", 1000, 3000, 10000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        CreateOrderResponse response = orderService.saveOrder(1L, orderRequest);
        em.flush(); em.clear();

        // 응답 체크
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getSubscribeUrl()).isNotNull();

        Optional<Orders> orderOptional = ordersRepository.findById(response.getOrderId());
        assertThat(orderOptional).isNotEmpty();
        Orders order = orderOptional.get();

        //주문 데이터 체크
        assertThat(order)
                .satisfies(o -> assertThat(o.getId()).isNotNull())
                .extracting(
                        Orders::getUserId, Orders::getUsedCouponId, Orders::getStatus, Orders::getDeliveryAddress, Orders::getOriginPrice,
                        Orders::getProdDiscount, Orders::getCouponDiscount, Orders::getPointDiscount, Orders::getAmountToPay
                )
                        .containsExactlyInAnyOrder(
                                1L, 1L, "PENDING", "서울시 테헤란로 123", 9000L,
                                900L, 1000L, 200L, 6900L
                        );
        //주문 상품 데이터 체크
        assertThat(order.getOrderItems())
                .allSatisfy(item -> assertThat(item.getId()).isNotNull())
                .extracting(OrderItems::getProductId, OrderItems::getProductVariantId, OrderItems::getProductName,
                        OrderItems::getUnitPrice, OrderItems::getDiscountRate,
                        OrderItems::getDiscountedPrice, OrderItems::getLineTotal, OrderItems::getQuantity, OrderItems::getThumbnail)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, "상품1",
                                3000L, 10, 2700L, 8100L, 3, "http://test.jpg"));

        //발행된 메시지 체크
        List<PendingOrderCreatedEvent> events = testListener.getEvents();
        assertThat(events).isNotEmpty();
        PendingOrderCreatedEvent ev = events.get(0);
        assertThat(ev)
                .satisfies(e ->{
                    assertThat(e.getOrderId()).isNotNull();
                    assertThat(e.getCreatedAt()).isNotNull();
                })
                .extracting(PendingOrderCreatedEvent::getUserId, PendingOrderCreatedEvent::getCouponId,
                        PendingOrderCreatedEvent::getUsedPoint, PendingOrderCreatedEvent::getAmountToPay,
                        PendingOrderCreatedEvent::getStatus)
                .containsExactlyInAnyOrder(1L, 1L, 200L, 6900L, "PENDING");
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