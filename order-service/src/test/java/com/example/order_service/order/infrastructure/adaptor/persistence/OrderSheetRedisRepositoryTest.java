package com.example.order_service.order.infrastructure.adaptor.persistence;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.WithRedis;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@MockKafka
@WithRedis
class OrderSheetRedisRepositoryTest {

    private static final String PREFIX_ORDER_SHEET = "order:sheet:";

    @Autowired
    private OrderSheetRedisRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IdGenerator idGenerator;

    private final PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));

    @Test
    @DisplayName("주문서를 redis에 저장한다.")
    void save() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        Duration ttl = Duration.ofMinutes(30);
        //when
        OrderSheet save = repository.save(orderSheet, ttl);
        //then
        String redisKey = PREFIX_ORDER_SHEET + save.getId();
        String savedString = redisTemplate.opsForValue().get(redisKey);

        assertThat(savedString).isNotNull();
        assertThat(save.getId()).isEqualTo(extractValue(savedString, "$.id"));

        Long expireTime = redisTemplate.getExpire(redisKey);
        assertThat(expireTime).isGreaterThan(0L);
        assertThat(expireTime).isLessThanOrEqualTo(ttl.getSeconds());
    }

    @Test
    @DisplayName("redis에서 주문서 아이디로 주문서를 조회한다")
    void findById() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        Duration ttl = Duration.ofMinutes(30);
        OrderSheet save = repository.save(orderSheet, ttl);
        //when
        Optional<OrderSheet> findOrderSheet = repository.findById(save.getId());
        //then
        assertThat(findOrderSheet).isPresent();
        assertThat(findOrderSheet.get().getId()).isEqualTo(save.getId());
    }

    @Test
    @DisplayName("redis에서 주문서를 찾을 수 없으면 빈 Optional 이 반환된다")
    void findById_notFound() {
        //given
        //when
        Optional<OrderSheet> findOrderSheet = repository.findById(999L);
        //then
        assertThat(findOrderSheet).isEmpty();
    }

    @Test
    @DisplayName("redis에서 주문서를 사용자 아이디와 주문서 아이디로 조회한다")
    void findByIdAndOrdererId() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        Duration ttl = Duration.ofMinutes(30);
        OrderSheet save = repository.save(orderSheet, ttl);
        //when
        Optional<OrderSheet> findOrderSheet = repository.findByIdAndOrdererId(save.getId(), save.getOrderer().getUserId());
        //then
        assertThat(findOrderSheet).isPresent();
        assertThat(findOrderSheet.get().getId()).isEqualTo(save.getId());
        assertThat(findOrderSheet.get().getOrderer().getUserId()).isEqualTo(save.getOrderer().getUserId());
    }

    @Test
    @DisplayName("redis에서 주문서를 찾을 수 없으면 빈 Optional이 반환된다")
    void findByIdAndOrdererId_notFound() {
        //given
        //when
        Optional<OrderSheet> findOrderSheet = repository.findByIdAndOrdererId(999L, 1L);
        //then
        assertThat(findOrderSheet).isEmpty();
    }

    private OrderSheet createOrderSheet(LocalDateTime expiresAt) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", couponDiscountPolicy, 1);
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "장바구니 1000원 할인", couponDiscountPolicy, Money.wons(10000L));

        OrderSheetItem orderSheetItem = OrderSheetItem.create(product, price, 5, options, idGenerator);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(orderSheetItem), expiresAt, idGenerator);

        orderSheet.changeShippingAddress(shippingAddress);
        orderSheet.applyItemCoupon(orderSheetItem.getId(), itemCoupon, pointUsagePolicy);
        orderSheet.applyCartCoupon(cartCoupon, pointUsagePolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointUsagePolicy);
        return orderSheet;
    }

    private <T> T extractValue(String jsonString, String path) {
        return JsonPath.read(jsonString, path);
    }
}