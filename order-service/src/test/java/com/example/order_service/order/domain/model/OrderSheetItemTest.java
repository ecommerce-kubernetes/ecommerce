package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderSheetItemTest {

    @Test
    @DisplayName("주문 항목을 생성한다")
    void create() {
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
    @DisplayName("주문서 항목의 주문 수량이 0 이하면 예외가 발생한다.")
    void create_quantity_less_than_1() {
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
    @DisplayName("주문 항목을 생성할때 상품 옵션이 누락되면 예외가 발생한다.")
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

    @Test
    @DisplayName("상품 정상가 총액을 계산한다.")
    void getOriginalLineTotal() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
        //when
        Money result = item.getOriginalLineTotal();
        //then
        assertThat(result).isEqualTo(priceSnapshot.getOriginalPrice().multiple(quantity));
    }

    @Test
    @DisplayName("상품 기본 할인 총액을 계산한다.")
    void getProductDiscountLineTotal(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
        //when
        Money result = item.getProductDiscountLineTotal();
        //then
        assertThat(result).isEqualTo(priceSnapshot.getDiscountAmount().multiple(quantity));
    }

    @Test
    @DisplayName("상품 판매가 총액을 계산한다.")
    void getLineTotal(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
        //when
        Money result = item.getLineTotal();
        //then
        assertThat(result).isEqualTo(priceSnapshot.getDiscountedPrice().multiple(quantity));
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰을 적용한다.")
    void applyItemCoupon(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());

        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", Money.wons(1000L));
        //when
        item.applyItemCoupon(itemCoupon);
        //then
        assertThat(item.getItemCouponSnapshot()).isEqualTo(itemCoupon);
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰을 적용할때 쿠폰 할인 금액은 상품 판매가 총액을 초과할 수 없다.")
    void applyItemCoupon_exceed_lineTotal(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());

        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "10000원 할인 쿠폰", Money.wons(10000L));
        //when
        //then
        assertThatThrownBy(() -> item.applyItemCoupon(itemCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ITEM_COUPON);
    }

    @Test
    @DisplayName("주문 항목의 최종 금액을 계산한다. (상품 쿠폰 미적용)")
    void getFinalAmount_not_applied_itemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
        //when
        Money result = item.getFinalAmount();
        //then
        assertThat(result).isEqualTo(item.getLineTotal());
    }

    @Test
    @DisplayName("주문 항목의 최종 금액을 계산한다. (상품 쿠폰 적용)")
    void getFinalAmount_applied_itemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", Money.wons(1000L));
        item.applyItemCoupon(itemCoupon);
        //when
        Money result = item.getFinalAmount();
        //then
        assertThat(result).isEqualTo(item.getLineTotal().subtract(itemCoupon.getDiscountAmount()));
    }
}
