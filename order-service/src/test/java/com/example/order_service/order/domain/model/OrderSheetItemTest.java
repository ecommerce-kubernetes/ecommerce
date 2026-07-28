package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
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

    private final IdGenerator idGenerator = new TsidGenerator();

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
        OrderSheetItem result = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, List.of(productOption), idGenerator);
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
        assertThatThrownBy(() -> OrderSheetItem.create(null, priceSnapshot, quantity, List.of(productOption), idGenerator))
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
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, null, quantity, List.of(productOption), idGenerator))
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
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, List.of(productOption), idGenerator))
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
        assertThatThrownBy(() -> OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, null, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 상품 옵션은 필수이다.");
    }

    @Test
    @DisplayName("주문 항목에 쿠폰을 적용한다.")
    void applyItemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 1);
        //when
        item.applyItemCoupon(itemCoupon);
        //then
        assertThat(item.getItemCouponSnapshot()).isEqualTo(itemCoupon);
    }
    
    @Test
    @DisplayName("주문 항목에 쿠폰을 적용할때 쿠폰이 없으면 예외가 발생한다.")
    void applyItemCoupon_itemCoupon_null() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        //then
        assertThatThrownBy(() -> item.applyItemCoupon(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용할 쿠폰 정보는 필수 입니다.");
    }
    
    @Test
    @DisplayName("상품 쿠폰을 해제한다")
    void removeItemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 1);

        item.applyItemCoupon(itemCoupon);
        //when
        item.removeItemCoupon();
        //then
        assertThat(item.getItemCouponSnapshot()).isNull();
    }

    @Test
    @DisplayName("상품 정상가 총액을 계산한다.")
    void calculateOriginalLineTotal() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        Money result = item.calculateOriginalLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(30000L));
    }

    @Test
    @DisplayName("상품 기본 할인 총액을 계산한다.")
    void calculateProductDiscountLineTotal(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        Money result = item.calculateProductDiscountLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(3000L));
    }

    @Test
    @DisplayName("상품 판매가 총액을 계산한다.")
    void calculateLineTotal(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        Money result = item.calculateLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(27000L));
    }

    @Test
    @DisplayName("주문 항목의 쿠폰 할인 금액을 계산한다.")
    void calculateCouponDiscount(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 1);
        item.applyItemCoupon(itemCoupon);
        //when
        Money couponDiscount = item.calculateCouponDiscount();
        //then
        assertThat(couponDiscount).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰이 적용되지 않은 경우 상품 쿠폰 할인 금액은 0원 이다.")
    void calculateCouponDiscount_not_apply_itemCoupon(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        Money couponDiscount = item.calculateCouponDiscount();
        //then
        assertThat(couponDiscount).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("주문 항목에 적용된 상품 쿠폰 할인 금액이 주문 항목의 판매가 총액을 넘어서는 경우 쿠폰 할인 금액은 판매가 총액을 한도로 적용된다.")
    void calculateCouponDiscount_lineTotal_lessThan_discountAmount(){
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 1;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(30000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "30000원 할인 쿠폰", policy, 1);
        item.applyItemCoupon(itemCoupon);
        //when
        Money result = item.calculateCouponDiscount();
        //then
        assertThat(result).isEqualTo(item.calculateLineTotal());
    }

    @Test
    @DisplayName("주문 항목의 최종 금액을 계산한다. (상품 쿠폰 미적용 시 원금액 반환)")
    void calculateFinalAmount_not_applied_itemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        //when
        Money result = item.calculateFinalAmount();
        //then
        assertThat(result).isEqualTo(item.calculateLineTotal());
    }

    @Test
    @DisplayName("주문 항목의 최종 금액을 계산한다. (상품 쿠폰 적용 시 할인액 차감)")
    void calculateFinalAmount_applied_itemCoupon() {
        //given
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderSheetItem item = OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList(), idGenerator);
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", policy, 1);
        item.applyItemCoupon(itemCoupon);
        //when
        Money result = item.calculateFinalAmount();
        //then
        assertThat(result).isEqualTo(Money.wons(26000L));
    }
}
