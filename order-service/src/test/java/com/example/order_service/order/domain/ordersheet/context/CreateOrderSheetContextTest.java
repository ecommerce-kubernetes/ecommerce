package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderSheetContextTest {

    @Test
    @DisplayName("주문자가 누락되면 예외가 발생한다.")
    void orderer_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        CreateOrderSheetItemContext item = CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .optionSnapshots(Collections.emptyList())
                .build();
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetContext.builder()
                .orderer(null)
                .shippingAddress(null)
                .items(List.of(item))
                .expiresAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 주문자는 필수이다.");
    }

    @Test
    @DisplayName("주문 항목이 누락되면 예외가 발생한다.")
    void items_null(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetContext.builder()
                .orderer(orderer)
                .shippingAddress(null)
                .items(null)
                .expiresAt(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 주문 항목은 필수이다.");
    }

    @Test
    @DisplayName("만료 시간이 누락되면 예외가 발생한다.")
    void expiresAt_null(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        CreateOrderSheetItemContext item = CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .optionSnapshots(Collections.emptyList())
                .build();
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        //when
        //then
        assertThatThrownBy(() -> CreateOrderSheetContext.builder()
                .orderer(orderer)
                .shippingAddress(null)
                .items(List.of(item))
                .expiresAt(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 만료 시간은 필수이다.");
    }
}