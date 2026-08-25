package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.*;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.support.annotation.IsolatedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IsolatedTest
@Transactional
public class OrderQueryServiceTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("주문 아이디와 주문자 아이디로 주문을 조회한다.")
    void getOrder() {
        //given
        Order order = OrderFixtureBuilder.given().build();
        orderRepository.save(order);
        flushAndClear();
        //when
        OrderResult findOrder = orderQueryService.getOrder(order.getId(), order.getOrderer().getUserId());
        //then
        assertThat(findOrder.orderId()).isEqualTo(order.getId());
        assertThat(findOrder.orderer()).isEqualTo(order.getOrderer());
        assertThat(findOrder.shippingAddress()).isEqualTo(order.getShippingAddress());
        assertThat(findOrder.orderItems()).hasSize(1);
    }

    @Test
    @DisplayName("주문을 찾을 수 없으면 예외가 발생한다.")
    void getOrder_whenOrderNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderQueryService.getOrder(999L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 목록을 조회한다.")
    void getOrders() {
        //given
        Order order1 = OrderFixtureBuilder.given().build();
        Order order2 = OrderFixtureBuilder.given().build();
        Pageable pageable = PageRequest.of(0, 10);
        OrderSearchCommand command = OrderSearchCommand.of("latest", null, null, pageable);
        orderRepository.save(order1);
        orderRepository.save(order2);
        flushAndClear();
        //when
        Page<OrderSummaryResult> orders = orderQueryService.getOrders(1L, command);
        //then
        assertThat(orders.getNumber()).isEqualTo(0);
        assertThat(orders.getTotalElements()).isEqualTo(2L);
        assertThat(orders.getContent()).hasSize(2)
                .extracting("orderId", "status")
                .containsExactlyInAnyOrder(
                        tuple(order1.getId(), order1.getStatus()),
                        tuple(order2.getId(), order2.getStatus())
                );
    }

    @Test
    @DisplayName("타임아웃된 대기중 주문을 조회한다.")
    void getOrdersByPendingAndCreatedAtBefore() {
        //given
        Order order1 = OrderFixtureBuilder.given().build();
        Order order2 = OrderFixtureBuilder.given().build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(40);
        em.createNativeQuery("UPDATE orders SET created_at = :pastTime WHERE id IN (:id)")
                .setParameter("pastTime", pastTime)
                .setParameter("id", order1.getId())
                .executeUpdate();

        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
        //when
        List<OrderSummaryResult> result = orderQueryService.getOrdersByPendingAndCreatedAtBefore(timeoutThreshold);
        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("orderId")
                .containsExactlyInAnyOrder(order1.getId());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
