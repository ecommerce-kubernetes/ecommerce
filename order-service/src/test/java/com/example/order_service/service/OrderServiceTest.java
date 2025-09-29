package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.InsufficientException;
import com.example.order_service.exception.NotFoundException;
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
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        saveOrder = new Orders(1L, 1L, "PENDING", "서울시 테헤란로 123", 3000L, 300L,
                1000L, 700L, 1000L);
        List<OrderItems> orderItems = List.of(new OrderItems(1L, 1L, "상품1", List.of(new ItemOptionResponse("색상", "RED")),
                1000L, 10, 900L, 2700L, 3, "http://test.jpg"));
        saveOrder.addOrderItems(orderItems);
        ordersRepository.save(saveOrder);
    }

    @Test
    @DisplayName("주문 생성 테스트-정상 처리")
    @Transactional
    void saveOrderTest_success(){
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
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenReturn(new CouponResponse("AMOUNT", 1000, 3000, 10000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        //서비스 실행
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
    @DisplayName("주문 생성 테스트-상품 서비스 404")
    void saveOrderTest_productService404(){
        when(productClientService.fetchProductByVariantIds(any()))
                .thenThrow(new NotFoundException("No products found for that option"));
        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No products found for that option");
    }

    @Test
    @DisplayName("주문 생성 테스트-유저 서비스 404")
    void saveOrderTest_userService404(){
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(List.of(new ProductResponse(1L, 1L, "상품1",
                        new ProductPrice(3000, 10, 300, 2700),
                        "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED")))));
        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenThrow(new NotFoundException("UserService 404"));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);
        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("UserService 404");
    }

    @Test
    @DisplayName("주문 생성 테스트-쿠폰 서비스 404")
    void saveOrderTest_couponService404(){
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(List.of(new ProductResponse(1L, 1L, "상품1",
                        new ProductPrice(3000, 10, 300, 2700),
                        "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED")))));

        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenReturn(new UserBalanceResponse(1L, 10000L, 3000L));
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("CouponService 404"));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest));
    }

    @Test
    @DisplayName("주문 생성 테스트-포인트 부족")
    void saveOrderTest_InsufficientPoint(){
        //상품 서비스 모킹
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(
                        List.of(new ProductResponse(1L, 1L, "상품1",
                                new ProductPrice(3000, 10, 300, 2700),
                                "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED"))))
                );
        //유저 서비스 모킹
        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenReturn(new UserBalanceResponse(1L, 10000L, 100L));
        //쿠폰 서비스 모킹
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenReturn(new CouponResponse("AMOUNT", 1000, 3000, 10000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("사용가능한 포인트가 부족");
    }

    @Test
    @DisplayName("주문 생성 테스트-최소 결제 금액 만족 실패")
    void saveOrderTest_InsufficientMinPurchaseAmount(){
        //상품 서비스 모킹
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(
                        List.of(new ProductResponse(1L, 1L, "상품1",
                                new ProductPrice(3000, 10, 300, 2700),
                                "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED"))))
                );
        //유저 서비스 모킹
        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenReturn(new UserBalanceResponse(1L, 10000L, 1000L));
        //쿠폰 서비스 모킹
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenReturn(new CouponResponse("AMOUNT", 1000, 10000, 20000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);
        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("결제 금액이 쿠폰 최소 결제 금액 미만");
    }

    @Test
    @DisplayName("주문 생성 테스트-예상 결제 금액과 실제 결제 금액이 맞지 않음")
    void saveOrderTest_expectedPriceNotMatched(){
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
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenReturn(new CouponResponse("AMOUNT", 1000, 3000, 10000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6700L);

        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("예상 결제 금액과 실제 결제 금액이 맞지 않습니다");
    }

    @Test
    @DisplayName("주문 생성 테스트-예상 결제 금액과 실제 결제 금액이 맞지 않음")
    void saveOrderTest_insufficientCash(){
        //상품 서비스 모킹
        when(productClientService.fetchProductByVariantIds(any()))
                .thenReturn(
                        List.of(new ProductResponse(1L, 1L, "상품1",
                                new ProductPrice(3000, 10, 300, 2700),
                                "http://test.jpg", List.of(new ItemOptionResponse("색상", "RED"))))
                );
        //유저 서비스 모킹
        when(userClientService.fetchBalanceByUserId(anyLong()))
                .thenReturn(new UserBalanceResponse(1L, 1000L, 3000L));
        //쿠폰 서비스 모킹
        when(couponClientService.fetchCouponByUserCouponId(anyLong(), anyLong()))
                .thenReturn(new CouponResponse("AMOUNT", 1000, 3000, 10000));

        OrderRequest orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123",
                1L,
                200L,
                6900L);

        assertThatThrownBy(() -> orderService.saveOrder(1L, orderRequest))
                .isInstanceOf(InsufficientException.class)
                .hasMessage("잔액이 부족합니다");
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