package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SagaManagerTest {

    @Autowired
    SagaManager sagaManager;
    @MockitoBean
    KafkaProducer kafkaProducer;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    OrderService orderService;
    @Container
    static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    @DisplayName("pending 주문 처리")
    void processPendingOrderSagaTest(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        //데이터 준비
        Orders orders = new Orders(1L, "PENDING", "서울시 테헤란로 123");
        orders.addOrderItems(List.of(new OrderItems(1L, 3)));
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123", 1L, 2500,
                200);
        ReflectionTestUtils.setField(orders, "id", orderId);
        LocalDateTime createdAt = LocalDateTime.now();
        ReflectionTestUtils.setField(orders, "createAt", createdAt);
        PendingOrderCreatedEvent pendingOrderCreatedEvent = new PendingOrderCreatedEvent(OrderService.class, orders, orderRequest);

        //로직 실행
        sagaManager.processPendingOrderSaga(pendingOrderCreatedEvent);

        //결과 확인
        //1. redis Hash Data
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries("saga:order:" + orderId);
        assertThat(entries.get("status")).isEqualTo("PENDING");
        assertThat(((Number) entries.get("orderId")).longValue()).isEqualTo(orderId);
        assertThat(entries.get("createdAt")).isEqualTo(createdAt.toString());

        //2. redis ZSet Data
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", orderId);
        assertThat(score).isNotNull();

        //3. kafkaProducer 호출 확인
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaProducer).sendMessage(eq("order.created"), captor.capture());

        OrderCreatedEvent value = captor.getValue();
        assertThat(value.getOrderId()).isEqualTo(orderId);
        assertThat(value.getOrderProductList())
                .extracting(OrderProduct::getProductVariantId, OrderProduct::getStock)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
        assertThat(value.getUserCouponId()).isEqualTo(1L);
        assertThat(value.getReservedCashAmount()).isEqualTo(2500);
        assertThat(value.getReservedPointAmount()).isEqualTo(200);
        assertThat(value.getExpectTotalAmount()).isEqualTo(2700);
    }

    @Test
    @DisplayName("saga 성공 처리 1. PENDING 상태일때")
    void processSagaSuccessTest_PENDING(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        redisTemplate.opsForHash().put("saga:order:" + orderId, "status", "PENDING");
        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId,
                List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg", new PriceInfo(3000, 10, 300, 2700),
                        3, List.of(new ItemOption("색상", "RED")))));

        sagaManager.processSagaSuccess(event);

        Map<Object, Object> entries = redisTemplate.opsForHash().entries("saga:order:" + orderId);

        // redis hash "product" 필드 저장 확인
        assertThat(entries.size()).isEqualTo(2);
        assertThat(entries.containsKey("product")).isTrue();

        // 저장된 메시지 확인
        ProductStockDeductedEvent product = (ProductStockDeductedEvent) entries.get("product");
        assertThat(product.getOrderId()).isEqualTo(orderId);
        assertThat(product.getDeductedProducts())
                .extracting(DeductedProduct::getProductId, DeductedProduct::getProductVariantId,
                        DeductedProduct::getProductName, DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, "상품1", 3)
                );

        assertThat(product.getDeductedProducts())
                .extracting(DeductedProduct::getPriceInfo)
                .extracting(PriceInfo::getPrice, PriceInfo::getDiscountRate, PriceInfo::getDiscountAmount, PriceInfo::getFinalPrice)
                .containsExactlyInAnyOrder(
                        tuple(3000, 10, 300L, 2700L)
                );
    }

    @Test
    @DisplayName("saga 성공 처리 2. CANCELLED 상태일때")
    void processSagaSuccessTest_CANCELLED(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        redisTemplate.opsForHash().put("saga:order:" + orderId, "status", "CANCELLED");

        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId,
                List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg", new PriceInfo(3000, 10, 300, 2700),
                        3, List.of(new ItemOption("색상", "RED")))));

        sagaManager.processSagaSuccess(event);

        Map<Object, Object> entries = redisTemplate.opsForHash().entries("saga:order:" + orderId);

        //redis hash 저장되지 않는지 확인 -> 저장되지 않아야 함
        assertThat(entries).hasSize(1);
        assertThat(entries.containsKey("product")).isFalse();

        //롤백 처리 확인
        ArgumentCaptor<ProductStockDeductedEvent> captor = ArgumentCaptor.forClass(ProductStockDeductedEvent.class);
        verify(kafkaProducer).sendMessage(eq("product.stock.restore"), captor.capture());

        ProductStockDeductedEvent value = captor.getValue();

        assertThat(value.getOrderId()).isEqualTo(orderId);
        assertThat(value.getDeductedProducts())
                .extracting(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
    }

    @Test
    @DisplayName("saga 성공 처리 3. 검증 처리 성공")
    void processSagaSuccessTest_finalizedOrderSuccess(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        doNothing().when(orderService).finalizeOrder(any());
        Map<String, String> dataMap = Map.of("status", "PENDING", "user", "MockValue",
                "coupon", "MockValue");
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, dataMap);

        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId,
                List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg", new PriceInfo(3000, 10, 300, 2700),
                        3, List.of(new ItemOption("색상", "RED")))));

        sagaManager.processSagaSuccess(event);

        Object status = redisTemplate.opsForHash().get("saga:order:" + orderId, "status");
        assertThat(status).isNull();
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", orderId);
        assertThat(score).isNull();
    }

    @Test
    @DisplayName("saga 성공 처리 4. 검증 처리 실패")
    void processSagaSuccessTest_finalizedOrderFail(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        doThrow(new OrderVerificationException("exception"))
                .when(orderService).finalizeOrder(any());
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(orderId, 1L, true, 200, 2500, 2700);
        CouponUsedSuccessEvent couponEvent = new CouponUsedSuccessEvent(orderId, 1L, DiscountType.AMOUNT, 3000, 10000, 50000);
        Map<String, Object> dataMap = Map.of("status", "PENDING", "user", userEvent,
                "coupon", couponEvent);
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, dataMap);

        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId,
                List.of(new DeductedProduct(1L, 1L, "상품1", "http://test.jpg", new PriceInfo(3000, 10, 300, 2700),
                        3, List.of(new ItemOption("색상", "RED")))));

        sagaManager.processSagaSuccess(event);

        // redis 정리
        Object status = redisTemplate.opsForHash().get("saga:order:" + orderId, "status");
        assertThat(status).isEqualTo("CANCELLED");
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", orderId);
        assertThat(score).isNull();

        //kafkaMessage
        ArgumentCaptor<ProductStockDeductedEvent> productCaptor = ArgumentCaptor.forClass(ProductStockDeductedEvent.class);
        ArgumentCaptor<UserCashDeductedEvent> userCaptor = ArgumentCaptor.forClass(UserCashDeductedEvent.class);
        ArgumentCaptor<CouponUsedSuccessEvent> couponCaptor = ArgumentCaptor.forClass(CouponUsedSuccessEvent.class);

        verify(kafkaProducer).sendMessage(eq("product.stock.restore"), productCaptor.capture());
        verify(kafkaProducer).sendMessage(eq("user.cache.restore"), userCaptor.capture());
        verify(kafkaProducer).sendMessage(eq("coupon.used.cancel"), couponCaptor.capture());

        ProductStockDeductedEvent productRollback = productCaptor.getValue();

        assertThat(productRollback.getOrderId()).isEqualTo(orderId);
        assertThat(productRollback.getDeductedProducts())
                .extracting(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );

        UserCashDeductedEvent userRollback = userCaptor.getValue();
        assertThat(userRollback.getOrderId()).isEqualTo(orderId);
        assertThat(userRollback)
                .extracting(UserCashDeductedEvent::getUserId, UserCashDeductedEvent::getReservedPointAmount,
                        UserCashDeductedEvent::getReservedCashAmount, UserCashDeductedEvent::getExpectTotalAmount)
                .containsExactlyInAnyOrder(
                        1L, 200, 2500, 2700
                );

        CouponUsedSuccessEvent couponRollback = couponCaptor.getValue();
        assertThat(couponRollback.getOrderId()).isEqualTo(orderId);
        assertThat(couponRollback.getUserCouponId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("saga 실패 처리 PENDING 인 경우")
    void processSagaFailureTest_CANCELLED(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        doNothing().when(orderService).failOrder(anyLong());
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(orderId, 1L, true, 200, 2500, 2700);

        Map<String, Object> dataMap = Map.of("status", "PENDING", "user", userEvent);
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, dataMap);

        FailedEvent failedEvent = new FailedEvent(orderId, "out of Stock");

        sagaManager.processSagaFailure(failedEvent);

        Object status = redisTemplate.opsForHash().get("saga:order:" + orderId, "status");
        assertThat(status).isEqualTo("CANCELLED");
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", orderId);
        assertThat(score).isNull();

        ArgumentCaptor<UserCashDeductedEvent> userCaptor = ArgumentCaptor.forClass(UserCashDeductedEvent.class);
        verify(kafkaProducer).sendMessage(eq("user.cache.restore"), userCaptor.capture());

        UserCashDeductedEvent userRollback = userCaptor.getValue();

        assertThat(userRollback.getOrderId()).isEqualTo(orderId);
        assertThat(userRollback)
                .extracting(UserCashDeductedEvent::getUserId, UserCashDeductedEvent::getReservedPointAmount,
                        UserCashDeductedEvent::getReservedCashAmount, UserCashDeductedEvent::getExpectTotalAmount)
                .containsExactlyInAnyOrder(
                        1L, 200, 2500, 2700
                );
    }
}