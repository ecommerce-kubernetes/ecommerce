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

    private CreateOrderContext createOrderContext() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(10L, "장바구니 1000원 할인 쿠폰");

        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(1L, "1000원 할인 쿠폰");
        OrderItemAmount orderItemAmount = OrderItemAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(27000L),
                Money.wons(1000L),
                Money.wons(26000L)
        );

        CreateOrderItemContext itemContext = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(appliedItemCoupon)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(1000L),
                Money.wons(1000L), Money.wons(1000L), Money.wons(24000L));

        return CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(itemContext))
                .appliedCartCoupon(appliedCartCoupon)
                .orderAmount(orderAmount)
                .build();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
