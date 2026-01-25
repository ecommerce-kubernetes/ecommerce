package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.order_service.api.support.fixture.OrderRepositoryTestFixture.testOrderBuilder;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class OrderRepositoryTest extends ExcludeInfraTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository repository;

    @Test
    @DisplayName("주문 번호로 주문을 조회한다")
    void findByOrderNoTest(){
        //given
        Long userId = 1L;
        Order order = testOrderBuilder()
                .userId(userId)
                .addItem("상품1").addItem("상품2")
                .build();

        Order savedOrder = repository.save(order);
        //when
        Optional<Order> find = repository.findByOrderNo(savedOrder.getOrderNo());
        //then
        assertThat(find).isNotEmpty();

//        assertThat(find.get().getUserId()).isEqualTo(userId);
        assertThat(find.get().getOrderNo()).isNotNull();
    }

    @Test
    @DisplayName("특정 유저의 주문을 최신순으로 조회한다")
    void findByUserIdAndCondition_latest(){
        //given
        Long userId = 1L;
        Long otherUserId = 2L;
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .build();

        Order order1 = testOrderBuilder()
                .userId(userId)
                .addItem("상품1").addItem("상품2")
                .build();

        Order order2 = testOrderBuilder()
                .userId(userId)
//                .status(OrderStatus.COMPLETED)
                .addItem("상품1").addItem("상품3")
                .build();

        Order order3 = testOrderBuilder()
                .userId(otherUserId)
                .addItem("상품1").addItem("상품2").addItem("상품3")
                .build();

        repository.save(order1);
        repository.save(order2);
        repository.save(order3);
        //when
        Page<Order> result = repository.findByUserIdAndCondition(userId, condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Order::getId)
                .containsExactly(order2.getId(), order1.getId());

//        assertThat(result.getContent())
//                .extracting(Order::getUserId)
//                .containsOnly(userId);
    }


    @Test
    @DisplayName("특정 유저의 주문을 오래된 순으로 조회한다")
    void findByUserIdAndCondition_oldest(){
        //given
        Long userId = 1L;
        Long otherUserId = 2L;
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("oldest")
                .build();

        Order order1 = testOrderBuilder()
                .userId(userId)
                .addItem("상품1").addItem("상품2")
                .build();

        Order order2 = testOrderBuilder()
                .userId(userId)
//                .status(OrderStatus.COMPLETED)
                .addItem("상품1").addItem("상품3")
                .build();

        Order order3 = testOrderBuilder()
                .userId(otherUserId)
                .addItem("상품1").addItem("상품2").addItem("상품3")
                .build();
        repository.save(order1);
        repository.save(order2);
        repository.save(order3);
        //when
        Page<Order> result = repository.findByUserIdAndCondition(userId, condition);
        //then
        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Order::getId)
                .containsExactly(order1.getId(), order2.getId());
    }

    @Test
    @DisplayName("특정 유저의 주문중 해당 년도에 주문한 주문을 조회한다")
    void findByUserIdAndCondition_match_year(){
        //given
        Long userId = 1L;
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .year("2024")
                .build();

        Order order1 = testOrderBuilder()
                .userId(userId)
                .addItem("상품1").addItem("상품2")
                .build();

        Order order2 = testOrderBuilder()
                .userId(userId)
//                .status(OrderStatus.COMPLETED)
                .addItem("상품1").addItem("상품3")
                .build();

        repository.save(order1);
        repository.save(order2);

        orderTimeSetting(order1.getId(), LocalDateTime.of(2024, 10, 20, 10, 5, 10));
        orderTimeSetting(order2.getId(), LocalDateTime.of(2025, 10, 20, 10, 5, 10));
        //when
        Page<Order> result = repository.findByUserIdAndCondition(userId, condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Order::getId)
                .containsExactly(order1.getId());
    }

    @Test
    @DisplayName("특정 유저의 주문중 주문 상품이 동일한 주문을 조회한다")
    void findByUserIdAndCondition_match_productName(){
        //given
        Long userId = 1L;
        OrderSearchCondition condition = OrderSearchCondition.builder()
                .page(1)
                .size(10)
                .sort("latest")
                .productName("상품1")
                .build();

        Order order1 = testOrderBuilder()
                .userId(userId)
                .addItem("상품1").addItem("상품2")
                .build();

        Order order2 = testOrderBuilder()
                .userId(userId)
//                .status(OrderStatus.COMPLETED)
                .addItem("상품1").addItem("상품3")
                .build();

        Order order3 = testOrderBuilder()
                .userId(userId)
//                .status(OrderStatus.COMPLETED)
                .addItem("상품3")
                .build();

        repository.save(order1);
        repository.save(order2);
        repository.save(order3);
        //when
        Page<Order> result = repository.findByUserIdAndCondition(userId, condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Order::getId)
                .containsExactly(order2.getId(), order1.getId());
    }

    private void orderTimeSetting(Long orderId, LocalDateTime targetTime) {
        em.flush();
        em.createNativeQuery("update orders set created_at = :targetTime where id = :orderId")
                        .setParameter("targetTime", targetTime)
                                .setParameter("orderId", orderId)
                                        .executeUpdate();
        em.clear();
    }
}
