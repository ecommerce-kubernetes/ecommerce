package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderSheetItemTest {

    @Test
    @DisplayName("주문 항목을 생성한다")
    void create(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        int quantity = 1;
        //when
        OrderSheetItem result = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, List.of(productOption));
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(OrderSheetItem::getProductSnapshot, OrderSheetItem::getPriceSnapshot, OrderSheetItem::getQuantity)
                .containsExactly(
                        productSnapshot, priceSnapshot, quantity
                );
        assertThat(result.getOptionSnapshots())
                .containsExactly(productOption);
    }
    
    @Test
    @DisplayName("주문 항목을 생성할때 상품 스냅샷이 누락되면 예외가 발생한다.")
    void create_productSnapshot_null() {
        //given
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        int quantity = 1;
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(null, priceSnapshot, quantity, List.of(productOption)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 정보는 필수이다.");
    }

    @Test
    @DisplayName("주문 항목을 생성할때 가격 스냅샷이 누락되면 예외가 발생한다.")
    void create_priceSnapshot_null() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        int quantity = 1;
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, null, quantity, List.of(productOption)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 가격은 필수이다.");
    }

    @Test
    @DisplayName("주문서 상품의 주문 수량이 0 이하면 예외가 발생한다.")
    void create_quantity_less_than_1(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        int quantity = 0;
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, List.of(productOption)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("주문 항목을 생성할때 상품 옵션이 누락되면 예외가 발생한다")
    void create_optionSnapshots_null() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 옵션은 필수이다.");
    }
}
