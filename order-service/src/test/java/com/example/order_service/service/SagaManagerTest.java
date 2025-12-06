package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.example.order_service.service.kafka.KafkaProducer;
import com.example.order_service.api.order.domain.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
    @Autowired
    ObjectMapper mapper;
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
        LocalDateTime createdAt = LocalDateTime.now();

        PendingOrderCreatedEvent pendingOrderCreatedEvent = new PendingOrderCreatedEvent(OrderService.class, orderId, 1L, 1L, 200L, 6900L, "PENDING", createdAt,
                Map.of(1L, 3));

        sagaManager.processPendingOrderSaga(pendingOrderCreatedEvent);

        //결과 확인
        //1. redis Hash Data
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries("saga:order:" + orderId);
        assertThat(entries.get("status")).isEqualTo("PENDING");
        assertThat(((Number) entries.get("orderId")).longValue()).isEqualTo(orderId);
        assertThat(entries.get("createdAt")).isEqualTo(createdAt.toString());

        //2. redis set data
        Long steps = redisTemplate.opsForSet().size("saga:steps:" + orderId);
        assertThat(steps).isEqualTo(3);

        //3. redis ZSet Data
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", String.valueOf(orderId));
        assertThat(score).isNotNull();

        //4. kafkaProducer 호출 확인
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaProducer).sendMessage(eq("order.created"), anyString(), captor.capture());

        OrderCreatedEvent value = captor.getValue();
        assertThat(value.getOrderId()).isEqualTo(orderId);
        assertThat(value.getDeductedProducts())
                .extracting(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
        assertThat(value.getUserCouponId()).isEqualTo(1L);
        assertThat(value.getReservedCashAmount()).isEqualTo(6900);
        assertThat(value.getReservedPointAmount()).isEqualTo(200);
        assertThat(value.getExpectTotalAmount()).isEqualTo(7100);
    }

    @Test
    @DisplayName("saga 성공 처리 1. PENDING 상태일때")
    void processSagaSuccessTest_PENDING(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        LocalDateTime createdAt = LocalDateTime.now();
        Map<String, Object> initialState = Map.of("orderId", orderId, "status", "PENDING",
                "createdAt", createdAt.toString());
        List<String> requiredField = List.of("product", "coupon", "user");
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, initialState);
        redisTemplate.opsForSet().add("saga:steps:" + orderId, requiredField.toArray(new String[0]));

        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId, List.of(new DeductedProduct(1L, 3)));

        sagaManager.processSagaSuccess(event);

        //응답 셋 확인
        Long size = redisTemplate.opsForSet().size("saga:steps:" + orderId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("saga:order:" + orderId);
        assertThat(size).isEqualTo(2);

        // redis hash "product" 필드 저장 확인
        assertThat(entries.containsKey("product")).isTrue();

        // 저장된 메시지 확인
        ProductStockDeductedEvent product = mapper.convertValue(entries.get("product"), ProductStockDeductedEvent.class);

        assertThat(product.getOrderId()).isEqualTo(orderId);
        assertThat(product.getDeductedProducts())
                .extracting(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
    }

    @Test
    @DisplayName("saga 성공 처리 2. CANCELLED 상태일때")
    void processSagaSuccessTest_CANCELLED(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        redisTemplate.opsForHash().put("saga:order:" + orderId, "status", "CANCELLED");

        ProductStockDeductedEvent event = new ProductStockDeductedEvent(orderId,
                List.of(new DeductedProduct(1L, 3)));

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
    @DisplayName("saga 성공 처리 3. 모든 응답 도착")
    void processSagaSuccessTest_COMPLETED(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        doNothing().when(orderService).completeOrder(orderId);
        List<String> requiredField = new ArrayList<>();
        ProductStockDeductedEvent productEvent = new ProductStockDeductedEvent(orderId, List.of(new DeductedProduct(1L, 3)));
        long score = TimeUnit.MINUTES.toMillis(5) + System.currentTimeMillis();
        requiredField.add("product");
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(orderId, 1L, true, 200, 2500, 2700);
        CouponUsedSuccessEvent couponEvent = new CouponUsedSuccessEvent(orderId, 1L, DiscountType.AMOUNT, 1000, 100, 10000);
        Map<String, Object> dataMap = Map.of("status", "PENDING", "user", userEvent, "coupon", couponEvent);
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, dataMap);
        redisTemplate.opsForSet().add("saga:steps:" + orderId, requiredField.toArray(new String[0]));
        redisTemplate.opsForZSet().add("saga:timeouts",  String.valueOf(orderId), score);

        sagaManager.processSagaSuccess(productEvent);

        assertThat(redisTemplate.hasKey("saga:order:" + orderId)).isFalse();
        assertThat(redisTemplate.hasKey("saga:steps:" + orderId)).isFalse();
        assertThat(redisTemplate.opsForZSet().score("saga:timeouts",  String.valueOf(orderId))).isNull();
    }

    @Test
    @DisplayName("saga 실패 처리 PENDING 인 경우")
    void processSagaFailureTest_PENDING(){
        Long orderId = (long) ((Math.random() * 100) + 1);
        doNothing().when(orderService).cancelOrder(orderId);
        List<String> requiredField = new ArrayList<>();
        FailedEvent productEvent = new FailedEvent(orderId, "out of stock");
        long score = TimeUnit.MINUTES.toMillis(5) + System.currentTimeMillis();
        requiredField.add("product");
        UserCashDeductedEvent userEvent = new UserCashDeductedEvent(orderId, 1L, true, 200, 2500, 2700);
        CouponUsedSuccessEvent couponEvent = new CouponUsedSuccessEvent(orderId, 1L, DiscountType.AMOUNT, 1000, 100, 10000);
        Map<String, Object> dataMap = Map.of("status", "PENDING", "user", userEvent, "coupon", couponEvent);
        redisTemplate.opsForHash().putAll("saga:order:" + orderId, dataMap);
        redisTemplate.opsForSet().add("saga:steps:" + orderId, requiredField.toArray(new String[0]));
        redisTemplate.opsForZSet().add("saga:timeouts",  String.valueOf(orderId), score);

        sagaManager.processSagaFailure(productEvent);

        Map<Object, Object> entries = redisTemplate.opsForHash().entries("saga:order:" + orderId);

        assertThat(entries.get("status")).isEqualTo("CANCELLED");
        assertThat(redisTemplate.hasKey("saga:steps:" + orderId)).isFalse();
        assertThat(redisTemplate.opsForZSet().score("saga:timeouts", String.valueOf(orderId))).isNull();

        ArgumentCaptor<UserCashDeductedEvent> userCaptor = ArgumentCaptor.forClass(UserCashDeductedEvent.class);
        ArgumentCaptor<CouponUsedSuccessEvent> couponCaptor = ArgumentCaptor.forClass(CouponUsedSuccessEvent.class);

        verify(kafkaProducer).sendMessage(eq("user.cash.restore"), eq(String.valueOf(orderId)), userCaptor.capture());
        verify(kafkaProducer).sendMessage(eq("coupon.used.cancel"), eq(String.valueOf(orderId)), couponCaptor.capture());

        UserCashDeductedEvent userRollback = userCaptor.getValue();
        CouponUsedSuccessEvent couponRollback = couponCaptor.getValue();

        assertThat(userRollback)
                .extracting(UserCashDeductedEvent::getOrderId, UserCashDeductedEvent::getUserId,
                        UserCashDeductedEvent::getReservedPointAmount, UserCashDeductedEvent::getReservedCashAmount)
                .containsExactlyInAnyOrder(orderId, 1L, 200L, 2500L);

        assertThat(couponRollback.getUserCouponId()).isEqualTo(1L);
    }
}