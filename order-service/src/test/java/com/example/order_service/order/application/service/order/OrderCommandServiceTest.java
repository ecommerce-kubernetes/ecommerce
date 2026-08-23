package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.domain.order.*;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.order.event.OrderAcceptedEvent;
import com.example.order_service.order.domain.order.event.OrderFailedEvent;
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
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
@RecordApplicationEvents
public class OrderCommandServiceTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("주문을 저장한다")
    void save() {
        //given
        CreateOrderContext context = createOrderContext();
        //when
        Long orderId = orderCommandService.saveOrder(context);
        flushAndClear();
        //then
        Optional<Order> order = orderRepository.findById(orderId);
        assertThat(order).isPresent();
    }

    @Test
    @DisplayName("주문을 저장할때 결제 금액이 0원이면 주문 접수로 변경한다.")
    void save_whenTotalPaymentAmountIsZero_thenAccept() {
        //given
        CreateOrderContext context = createZeroContext();
        //when
        Long orderId = orderCommandService.saveOrder(context);
        flushAndClear();
        //then
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(orderId).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("주문을 결제 완료로 변경한다.")
    void changeAccept() {
        //given
        IdGenerator idGenerator = new TsidGenerator();
        CreateOrderContext context = createOrderContext();
        Order order = orderRepository.save(Order.create(context, idGenerator));
        flushAndClear();
        //when
        orderCommandService.changeAccepted(order.getId());
        flushAndClear();
        //then
        Order findOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("주문을 결제 완료로 변경할때 주문을 찾을 수 없는 경우 예외가 발생한다.")
    void changeAccept_whenOrderNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderCommandService.changeAccepted(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문이 접수로 변경되면 주문 접수 이벤트가 발행된다.")
    void changeAccept_whenOrderChangeAccepted_thenPublishAcceptedEvent() {
        //given
        IdGenerator idGenerator = new TsidGenerator();
        CreateOrderContext context = createOrderContext();
        Order order = orderRepository.save(Order.create(context, idGenerator));
        flushAndClear();
        //when
        orderCommandService.changeAccepted(order.getId());
        flushAndClear();
        //then
        long eventCount = events.stream(OrderAcceptedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("주문을 완료한다.")
    void changeCompleted(){
        //given
        IdGenerator idGenerator = new TsidGenerator();
        CreateOrderContext context = createOrderContext();
        Order order = Order.create(context, idGenerator);
        order.accept();
        Order savedOrder = orderRepository.save(order);
        flushAndClear();
        //when
        orderCommandService.changeCompleted(savedOrder.getId());
        flushAndClear();
        //then
        Order findOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문을 완료할때 주문을 찾을 수 없으면 예외가 발생한다.")
    void changeCompleted_whenOrderNotFound_thenThrownException(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderCommandService.changeCompleted(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문을 실패로 변경한다.")
    void changeFailed(){
        //given
        IdGenerator idGenerator = new TsidGenerator();
        CreateOrderContext context = createOrderContext();
        Order order = Order.create(context, idGenerator);
        Order savedOrder = orderRepository.save(order);
        flushAndClear();
        //when
        orderCommandService.changeFailed(savedOrder.getId(), "주문 실패");
        flushAndClear();
        //then
        Order findOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(findOrder.getOrderCancelInfo().getReason()).isEqualTo("주문 실패");
    }

    @Test
    @DisplayName("주문을 실패로 변경하면 주문 실패 이벤트가 발행된다")
    void changeFailed_whenChangeFailed_thenPublishOrderFailedEvent(){
        //given
        IdGenerator idGenerator = new TsidGenerator();
        CreateOrderContext context = createOrderContext();
        Order order = Order.create(context, idGenerator);
        Order savedOrder = orderRepository.save(order);
        flushAndClear();
        //when
        orderCommandService.changeFailed(savedOrder.getId(), "주문 실패");
        flushAndClear();
        //then
        long eventCount = events.stream(OrderFailedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("주문을 실패로 변경할때 주문을 찾을 수 없으면 예외가 발생한다.")
    void changeFailed_whenOrderNotFound_thenThrownException(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderCommandService.changeFailed(999L, "주문 실패"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
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

    private static CreateOrderContext createZeroContext() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(1000L), 0, Money.ZERO, Money.wons(1000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(
                Money.wons(1000L),
                Money.ZERO,
                Money.wons(1000L),
                Money.ZERO,
                Money.wons(1000L)
        );
        CreateOrderItemContext itemContext = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(null)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(1000L), Money.ZERO, Money.ZERO,
                Money.ZERO, Money.wons(1000L), Money.ZERO);

        return CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(itemContext))
                .appliedCartCoupon(null)
                .orderAmount(orderAmount)
                .build();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
