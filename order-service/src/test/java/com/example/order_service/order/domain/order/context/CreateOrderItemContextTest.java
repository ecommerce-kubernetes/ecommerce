package com.example.order_service.order.domain.order.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderItemContextTest {

    @Test
    @DisplayName("상품 정보 스냅샷이 누락되면 예외가 발생한다.")
    void productSnapshot_null(){
        //given
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderItemContext.builder()
                .productSnapshot(null)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderItem) 생성시 상품 정보는 필수이다.");
    }

    @Test
    @DisplayName("상품 가격 스냅샷이 누락되면 예외가 발생한다.")
    void priceSnapshot_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(null)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderItem) 생성시 상품 가격 정보는 필수이다.");
    }

    @Test
    @DisplayName("상품 옵션 정보가 누락되면 예외가 발생한다.")
    void options_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .options(null)
                .orderItemAmount(orderItemAmount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderItem) 생성시 상품 옵션은 필수이다.");
    }

    @Test
    @DisplayName("주문 항목 가격 정보가 누락되면 예외가 발생한다.")
    void orderItemAmount_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .options(Collections.emptyList())
                .orderItemAmount(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderItem) 생성시 주문 항목 가격 정보는 필수이다.");
    }
}