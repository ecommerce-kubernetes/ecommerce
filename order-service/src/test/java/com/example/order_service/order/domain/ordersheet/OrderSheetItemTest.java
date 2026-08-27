package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class OrderSheetItemTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문 항목을 생성한다")
    void create() {
        //given
        CreateOrderSheetItemContext context = createContext(1);
        //when
        OrderSheetItem result = OrderSheetItem.create(context, idGenerator);
        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result)
                .extracting(OrderSheetItem::getProductSnapshot, OrderSheetItem::getPriceSnapshot, OrderSheetItem::getQuantity)
                .containsExactly(
                        context.productSnapshot(), context.priceSnapshot(), context.quantity()
                );

        assertThat(result.getOptionSnapshots()).isEqualTo(context.optionSnapshots());
    }

    @Test
    @DisplayName("주문 항목을 생성할때 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_whenIdGeneratorIsNull_thenThrownException() {
        //given
        CreateOrderSheetItemContext context = createContext(1);
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문 항목 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_whenIdGeneratorGenerateNullId_thenThrownException() {
        //given
        CreateOrderSheetItemContext context = createContext(1);

        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(context, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목(OrderSheetItem) 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("주문서 항목의 주문 수량이 1 이하면 예외가 발생한다.")
    void create_whenQuantityLessThan1_thenThrownException() {
        //given
        CreateOrderSheetItemContext context = createContext(0);
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("주문 항목에 쿠폰을 적용한다.")
    void applyItemCoupon() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().build();

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 1);
        //when
        item.applyItemCoupon(itemCoupon);
        //then
        assertThat(item.getItemCouponSnapshot()).isEqualTo(itemCoupon);
    }

    @Test
    @DisplayName("주문 항목에 쿠폰을 적용할때 쿠폰이 없으면 예외가 발생한다.")
    void applyItemCoupon_whenItemCouponIsNull_thenThrownException() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().build();
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
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withFixedItemCoupon(100L, Money.wons(1000L), 1)
                .build();
        //when
        item.removeItemCoupon();
        //then
        assertThat(item.getItemCouponSnapshot()).isNull();
    }

    @Test
    @DisplayName("상품 정상가 총액을 계산한다.")
    void calculateOriginalLineTotal() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .withQuantity(3).build();
        //when
        Money result = item.calculateOriginalLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(30000L));
    }

    @Test
    @DisplayName("상품 기본 할인 총액을 계산한다.")
    void calculateItemDiscountLineTotal() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .withQuantity(3).build();
        //when
        Money result = item.calculateItemDiscountLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(3000L));
    }

    @Test
    @DisplayName("상품 판매가 총액을 계산한다.")
    void calculateLineTotal() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .withQuantity(3).build();
        //when
        Money result = item.calculateLineTotal();
        //then
        assertThat(result).isEqualTo(Money.wons(27000L));
    }

    @Test
    @DisplayName("주문 항목의 쿠폰 할인 금액을 계산한다.")
    void calculateCouponDiscount() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withFixedItemCoupon(1L, Money.wons(1000L), 1)
                .build();
        //when
        Money couponDiscount = item.calculateCouponDiscount();
        //then
        assertThat(couponDiscount).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("주문 항목의 수량이 쿠폰 적용 가능 수량을 초과하면 최대 적용 수량까지만 할인한다.")
    void calculateCouponDiscount_whenQuantityExceedsApplyQuantityLimit_thenApplyDiscountUpToLimit() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(2)
                .withFixedItemCoupon(1L, Money.wons(1000L), 1)
                .build();
        //when
        Money couponDiscount = item.calculateCouponDiscount();
        //then
        assertThat(couponDiscount).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰이 적용되지 않은 경우 상품 쿠폰 할인 금액은 0원 이다.")
    void calculateCouponDiscount_whenNotAppliedItemCoupon_thenItemCouponDiscountIsZero() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().build();
        //when
        Money couponDiscount = item.calculateCouponDiscount();
        //then
        assertThat(couponDiscount).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("상품 쿠폰 할인 금액이 주문 항목의 판매가 총액을 초과하면 판매가 총액까지만 할인한다.")
    void calculateCouponDiscount_whenItemCouponDiscountExceedLineTotal_thenLimitToLineTotal() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(1)
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .withFixedItemCoupon(1L, Money.wons(10000L), 1)
                .build();
        //when
        Money result = item.calculateCouponDiscount();
        //then
        assertThat(result).isEqualTo(Money.wons(9000L));
    }

    @Test
    @DisplayName("상품 쿠폰이 적용되지 않은 경우 주문 항목의 최종 금액은 판매가 총액이다.")
    void calculateFinalAmount_whenNotAppliedItemCoupon_thenReturnLineTotal() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(2)
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .build();
        //when
        Money result = item.calculateFinalAmount();
        //then
        assertThat(result).isEqualTo(Money.wons(18000L));
    }

    @Test
    @DisplayName("상품 쿠폰이 적용된 경우 주문 항목의 최종 금액은 판매가 총액에 쿠폰 할인을 적용한 금액이다.")
    void calculateFinalAmount_whenAppliedItemCoupon_thenAppliedCouponDiscount() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(1)
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .withFixedItemCoupon(1L, Money.wons(1000L), 1)
                .build();
        //when
        Money result = item.calculateFinalAmount();
        //then
        assertThat(result).isEqualTo(Money.wons(8000L));
    }

    @Test
    @DisplayName("상품 가격정보가 동일하면 예외가 발생하지 않는다.")
    void validatePriceNotChanged_whenProductPriceMatches_thenNotThrow() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(1)
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .build();

        ProductPriceSnapshot target = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        //when
        //then
        assertDoesNotThrow(() -> item.validatePriceNotChanged(target));
    }

    @Test
    @DisplayName("상품 가격 정보가 동일하지 않으면 예외가 발생한다.")
    void validatePriceNotChanged_whenProductPriceMismatch_thenThrownException() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(1)
                .withPriceSnapshot(
                        ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L))
                )
                .build();

        ProductPriceSnapshot target = ProductPriceSnapshot.of(Money.wons(10000L), 20, Money.wons(2000L), Money.wons(8000L));
        //when
        //then
        assertThatThrownBy(() -> item.validatePriceNotChanged(target))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.PRODUCT_PRICE_CHANGED);
    }

    @Test
    @DisplayName("쿠폰이 적용되지 않은 주문 항목에 쿠폰 검증을 수행하면 예외가 발생한다.")
    void validateItemCouponNotChanged_whenNotAppliedItemCoupon_thenThrownException() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().build();

        CouponDiscountPolicy newCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(2000L));
        ItemCouponSnapshot newItemCoupon = ItemCouponSnapshot.of(1L, "2000원 할인 쿠폰", newCouponPolicy, 1);
        //when
        //then
        assertThatThrownBy(() -> item.validateItemCouponNotChanged(newItemCoupon))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 주문 항목에는 쿠폰이 적용되어있지 않습니다.");
    }

    @Test
    @DisplayName("주문 항목에 적용된 상품 쿠폰과 아이디와 할인 정책, 적용 가능 최대 수량이 동일하면 예외가 발생하지 않는다.")
    void validateItemCouponNotChanged_whenItemCouponMatches_thenNotThrow() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().withFixedItemCoupon(1L, Money.wons(1000L), 1).build();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot target = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", couponPolicy, 1);
        //when
        //then
        assertDoesNotThrow(() -> item.validateItemCouponNotChanged(target));
    }

    @Test
    @DisplayName("주문 항목에 적용된 상품 쿠폰 아이디와 동일하지 않으면 예외가 발생한다")
    void validateItemCouponNotChanged_whenItemCouponIdMismatch_thenThrownException() {
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given().withFixedItemCoupon(1L, Money.wons(1000L), 1).build();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot target = ItemCouponSnapshot.of(2L, "1000원 할인 쿠폰", couponPolicy, 1);
        //when
        //then
        assertThatThrownBy(() -> item.validateItemCouponNotChanged(target))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검증하려는 쿠폰 ID가 주문 항목에 적용된 상품 쿠폰 ID와 일치하지 않습니다.");
    }

    @Test
    @DisplayName("상품 쿠폰 할인 정책이 동일하지 않으면 예외가 발생한다.")
    void validateItemCouponNotChanged_whenItemCouponPolicyMismatch_thenThrownException() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withFixedItemCoupon(1L, Money.wons(1000L), 1)
                .build();

        CouponDiscountPolicy newCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(2000L));
        ItemCouponSnapshot target = ItemCouponSnapshot.of(1L, "2000원 할인 쿠폰", newCouponPolicy, 1);
        //when
        //then
        assertThatThrownBy(() -> item.validateItemCouponNotChanged(target))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.COUPON_POLICY_CHANGED);
    }

    @Test
    @DisplayName("상품 쿠폰의 최대 적용 가능 수량이 동일하지 않으면 예외가 발생한다.")
    void validateItemCouponNotChanged_whenItemCouponApplyQuantityLimitMismatch_thenThrownException() {
        //given
        OrderSheetItem item = OrderSheetItemFixtureBuilder.given()
                .withQuantity(3)
                .withFixedItemCoupon(1L, Money.wons(1000L), 1)
                .build();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot target = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", couponPolicy, 2);
        //when
        //then
        assertThatThrownBy(() -> item.validateItemCouponNotChanged(target))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.COUPON_POLICY_CHANGED);
    }

    private CreateOrderSheetItemContext createContext(int quantity) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1_XL",
                "청바지", "/product/product/jean1.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot productOption = ProductOptionSnapshot.of("사이즈", "XL");
        return CreateOrderSheetItemContext
                .builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(quantity)
                .optionSnapshots(List.of(productOption))
                .build();
    }
}
