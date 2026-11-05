package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderSheetItemContextTest {

    @Test
    @DisplayName("상품 정보 스냅샷이 누락되면 예외가 발생한다.")
    void productSnapshot_null(){
        //given
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetItemContext.builder()
                .productSnapshot(null)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .optionSnapshots(Collections.emptyList())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 정보는 필수이다.");
    }

    @Test
    @DisplayName("상품 가격 스냅샷이 누락되면 예외가 발생한다.")
    void priceSnapshot_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(null)
                .quantity(3)
                .optionSnapshots(Collections.emptyList())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 가격은 필수이다.");
    }

    @Test
    @DisplayName("상품 옵션이 누락되면 예외가 발생한다.")
    void options_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .optionSnapshots(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 옵션은 필수이다.");
    }
}