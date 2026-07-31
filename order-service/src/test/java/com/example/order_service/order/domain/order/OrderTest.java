package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문을 생성한다.")
    void create() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();
        //when
        Order order = Order.create(context, idGenerator);
        //then
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderName()).isEqualTo("상품");
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order)
                .extracting("orderer", "shippingAddress", "appliedCartCoupon", "orderAmount")
                .containsExactly(orderer, shippingAddress, appliedCartCoupon, orderAmount);
    }

    @Test
    @DisplayName("주문을 생성할때 주문자가 누락되면 예외가 발생한다.")
    void create_orderer_null() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(null)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 주문자는 필수이다.");
    }

    @Test
    @DisplayName("주문을 생성할때 배송 정보가 누락되면 예외가 발생한다.")
    void create_shippingAddress_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(null)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 배송 정보는 필수이다.");
    }

    @Test
    @DisplayName("주문을 생성할때 주문 가격 정보가 누락되면 예외가 발생한다.")
    void create_orderAmount_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(null)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 주문 가격 정보는 필수이다.");
    }

    @Test
    @DisplayName("주문 생성시 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_id_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("주문을 생성할때 주문 항목은 비어있을 수 없다")
    void create_orderItems_empty() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1234호");
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(1L, "장바구니 1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );
        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(appliedCartCoupon)
                .items(Collections.emptyList())
                .orderAmount(orderAmount)
                .build();
        //when
        //then
        assertThatThrownBy(() -> Order.create(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }

    private CreateOrderItemContext createOrderItemContext() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(1L, "1000원 상품 할인 쿠폰");
        int quantity = 3;
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.wons(1000L), Money.wons(26000L));
        return CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(appliedItemCoupon)
                .quantity(quantity)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();
    }
}