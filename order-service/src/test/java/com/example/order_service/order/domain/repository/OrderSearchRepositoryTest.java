package com.example.order_service.order.domain.repository;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderItem;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@MockKafka
@MockRedis
@Transactional
public class OrderSearchRepositoryTest {

    @Autowired
    private OrderRepositoryDeprecated orderRepositoryDepreCated;
    @Autowired
    private OrderSearchRepository orderSearchRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("주문 목록을 조회한다")
    void searchOrders(){
        //given
        Order order1 = createOrder();
        Order order2 = createOrder();
        Order order3 = createOrder();
        ReflectionTestUtils.setField(order3.getOrderer(), "userId", 2L);
        orderRepositoryDepreCated.saveAll(List.of(order1, order2, order3));
        OrderSearchCommand command =null;
        Pageable pageable = PageRequest.of(0, 20);
        //when
        Page<Order> orders = orderSearchRepository.searchOrders(1L, command, pageable);
        //then
        assertThat(orders.getContent()).hasSize(2);
        assertThat(orders.getTotalElements()).isEqualTo(2);
        assertThat(orders.getContent()).extracting(o -> o.getOrderer().getUserId())
                .containsOnly(1L);
    }

    @Test
    @DisplayName("연도 필터로 주문 목록을 조회한다")
    void searchOrders_byYear(){
        //given
        Order order2024 = orderRepositoryDepreCated.save(createOrder());
        Order order2025 = orderRepositoryDepreCated.save(createOrder());
        em.createQuery("update Order o set o.createdAt = :date where o.id = :id")
                .setParameter("date", LocalDateTime.of(2024, 10, 10, 10, 10))
                .setParameter("id", order2024.getId())
                .executeUpdate();

        em.createQuery("update Order o set o.createdAt = :date where o.id = :id")
                .setParameter("date", LocalDateTime.of(2025, 10, 10, 10, 10))
                .setParameter("id", order2025.getId())
                .executeUpdate();
        em.flush();
        em.clear();
        OrderSearchCommand command = null;
        Pageable pageable = PageRequest.of(0, 20);
        //when
        Page<Order> orders = orderSearchRepository.searchOrders(1L, command, pageable);
        //then
        assertThat(orders.getContent()).hasSize(1);
        assertThat(orders.getContent()).extracting(BaseEntity::getCreatedAt)
                .allSatisfy(createdAt ->
                        assertThat(createdAt).isAfterOrEqualTo(LocalDateTime.of(2024, 1,1,0,0)));
    }

    @Test
    @DisplayName("주문 상품 필터로 주문 목록을 조회한다")
    void searchOrders_byProductName(){
        //given
        Order order1 = createOrder();
        OrderItem jean = order1.getOrderItems().get(0);
        ReflectionTestUtils.setField(jean.getProduct(), "productName", "청바지");
        Order order2 = createOrder();
        OrderItem shirts = order2.getOrderItems().get(0);
        ReflectionTestUtils.setField(shirts.getProduct(), "productName", "셔츠");
        orderRepositoryDepreCated.saveAll(List.of(order1, order2));
        OrderSearchCommand command = null;
        Pageable pageable = PageRequest.of(0, 10);
        //when
        Page<Order> orders = orderSearchRepository.searchOrders(1L, command, pageable);
        //then
        assertThat(orders.getContent()).hasSize(1);
        assertThat(orders.getContent().get(0).getId()).isEqualTo(order2.getId());
    }

    @Test
    @DisplayName("최신순으로 정렬하여 조회한다")
    void searchOrders_sortByLatest(){
        //given
        Order firstOrder = orderRepositoryDepreCated.save(createOrder());
        Order secondOrder = orderRepositoryDepreCated.save(createOrder());
        OrderSearchCommand command = null;
        Pageable pageable = PageRequest.of(0, 20);
        //when
        Page<Order> orders = orderSearchRepository.searchOrders(1L, command, pageable);
        //then
        assertThat(orders.getContent()).hasSize(2);
        assertThat(orders.getContent().get(0).getId()).isEqualTo(secondOrder.getId());
        assertThat(orders.getContent().get(1).getId()).isEqualTo(firstOrder.getId());
    }

    private Order createOrder() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        OrderItem orderItem = createOrderItem();
        return null;
    }

    private OrderItem createOrderItem() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지",
                "/product/product/jean_1.jpg");
        ProductPriceSnapshot productPrice = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 할인 쿠폰", Money.wons(1000L));
//        return OrderItem.create(productSnapshot, productPrice, itemCoupon, 1, List.of(xl, blue));
        return null;
    }
}
