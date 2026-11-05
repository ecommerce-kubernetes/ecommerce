package com.example.order_service.order.domain.order.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderContextTest {

    @Test
    @DisplayName("주문자가 누락되면 예외가 발생한다.")
    void orderer_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        CreateOrderItemContext itemCtx = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(null)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.ZERO, Money.ZERO, Money.ZERO, Money.wons(27000L));
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        //when
        //then
        assertThatThrownBy(() -> CreateOrderContext.builder()
                .orderer(null)
                .shippingAddress(shippingAddress)
                .items(List.of(itemCtx))
                .appliedCartCoupon(null)
                .orderAmount(orderAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 주문자는 필수이다.");
    }

    @Test
    @DisplayName("배송정보가 누락되면 예외가 발생한다.")
    void shippingAddress_null(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        CreateOrderItemContext itemCtx = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(null)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.ZERO, Money.ZERO, Money.ZERO, Money.wons(27000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(null)
                .items(List.of(itemCtx))
                .appliedCartCoupon(null)
                .orderAmount(orderAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 배송 정보는 필수이다.");
    }

    @Test
    @DisplayName("주문 항목이 누락되면 예외가 발생한다.")
    void items_null(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.ZERO, Money.ZERO, Money.ZERO, Money.wons(27000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(null)
                .appliedCartCoupon(null)
                .orderAmount(orderAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 주문 항목은 필수이다.");
    }

    @Test
    @DisplayName("주문 가격 정보는 필수이다.")
    void orderAmount_null(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        CreateOrderItemContext itemCtx = CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(null)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        //when
        //then
        assertThatThrownBy(() -> CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(List.of(itemCtx))
                .appliedCartCoupon(null)
                .orderAmount(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문(Order) 생성시 주문 가격 정보는 필수이다.");
    }
}