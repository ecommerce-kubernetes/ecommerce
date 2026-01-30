package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.support.fixture.order.OrderCommandFixture.anOrderSearchCondition;
import static com.example.order_service.api.support.fixture.order.OrderFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class OrderRepositoryTest extends ExcludeInfraTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository repository;

    @Test
    @DisplayName("특정 유저의 주문을 최신순으로 조회한다")
    void findByUserIdAndCondition_latest(){
        //given
        OrderSearchCondition condition = anOrderSearchCondition().sort("latest").build();
        OrderCreationContext context = anOrderCreationContext().build();
        Order order1 = repository.save(Order.create(context));
        Order order2 = repository.save(Order.create(context));
        Order order3 = repository.save(Order.create(context));
        //when
        Page<Order> result = repository.findByUserIdAndCondition(1L, condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(3);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(Order::getId)
                .containsExactly(
                        order3.getId(),
                        order2.getId(),
                        order1.getId()
                );
    }

    @Test
    @DisplayName("특정 유저의 주문을 오래된 순으로 조회한다")
    void findByUserIdAndCondition_oldest(){
        //given
        OrderSearchCondition condition = anOrderSearchCondition().sort("oldest").build();
        OrderCreationContext context = anOrderCreationContext().build();
        Order order1 = repository.save(Order.create(context));
        Order order2 = repository.save(Order.create(context));
        Order order3 = repository.save(Order.create(context));
        //when
        Page<Order> result = repository.findByUserIdAndCondition(1L, condition);
        //then
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(3);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(Order::getId)
                .containsExactly(
                        order1.getId(),
                        order2.getId(),
                        order3.getId()
                );
    }

    @Test
    @DisplayName("특정 유저의 주문중 해당 년도에 주문한 주문을 조회한다")
    void findByUserIdAndCondition_match_year(){
        //given
        Long userId = 1L;
        OrderSearchCondition condition = anOrderSearchCondition().year("2026").build();
        OrderCreationContext context = anOrderCreationContext().build();
        Order order1 = repository.save(Order.create(context));
        Order order2 = repository.save(Order.create(context));
        Order order3 = repository.save(Order.create(context));

        orderTimeSetting(order1.getId(), LocalDateTime.of(2024, 10, 20, 10, 5, 10));
        orderTimeSetting(order2.getId(), LocalDateTime.of(2026, 10, 20, 10, 5, 10));
        orderTimeSetting(order3.getId(), LocalDateTime.of(2026, 10, 20, 10, 10, 10));
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
                .containsExactly(
                        order3.getId(),
                        order2.getId()
                );
    }

    @Test
    @DisplayName("특정 유저의 주문중 주문 상품명이 동일한 주문을 조회한다")
    void findByUserIdAndCondition_match_productName(){
        //given
        OrderSearchCondition condition = anOrderSearchCondition().productName("상품1").build();
        OrderCreationContext prod1Context = anOrderCreationContext().orderItemCreationContexts(
                List.of(anOrderItemCreationContext().productSpec(anProductSpec().productName("상품1").build()).build())).build();
        OrderCreationContext prod2Context = anOrderCreationContext().orderItemCreationContexts(
                List.of(anOrderItemCreationContext().productSpec(anProductSpec().productName("상품2").build()).build())).build();
        Order order1 = repository.save(Order.create(prod1Context));
        Order order2 = repository.save(Order.create(prod2Context));
        //when
        Page<Order> result = repository.findByUserIdAndCondition(1L, condition);
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

    private void orderTimeSetting(Long orderId, LocalDateTime targetTime) {
        em.flush();
        em.createNativeQuery("update orders set created_at = :targetTime where id = :orderId")
                        .setParameter("targetTime", targetTime)
                                .setParameter("orderId", orderId)
                                        .executeUpdate();
        em.clear();
    }
}
