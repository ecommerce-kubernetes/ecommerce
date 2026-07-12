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
    @DisplayName("주문서 상품을 생성한다")
    void create(){
        //given
        String sheetItemId = "sheetItemId";
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        //when
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, 10, options);
        //then
        assertThat(item)
                .extracting("sheetItemId", "productSnapshot", "itemPrice", "itemCoupon", "options")
                .containsExactlyInAnyOrder(sheetItemId, product, price, itemCoupon, options);
    }

    @Test
    @DisplayName("주문서 상품의 주문 수량은 1이상이여야 한다")
    void create_quantity_less_than_1(){
        //given
        String sheetItemId = "sheetItemId";
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        //when
        //then
        assertThatThrownBy(() -> OrderSheetItem.create(sheetItemId, product, price, itemCoupon, 0, options))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.QUANTITY_MUST_BE_GREATER_THAN_ZERO);
    }

    @Test
    @DisplayName("주문 상품 라인 가격(상품 판매 가격 * 주문 수량)을 반환한다")
    void getProductLineTotal(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money productLineTotal = item.getProductLineTotal();
        //then
        assertThat(productLineTotal).isEqualTo(price.getDiscountedPrice().multiple(quantity));
    }

    @Test
    @DisplayName("주문 상품 적용 쿠폰 할인 금액을 반환한다")
    void getAppliedCouponDiscount(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money appliedCouponDiscount = item.getAppliedCouponDiscount();
        //then
        assertThat(appliedCouponDiscount).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("쿠폰 할인 금액이 주문 상품 라인 금액보다 크면 주문 상품 적용 쿠폰 할인 금액은 상품 라인 금액이다")
    void getAppliedCouponDiscount_couponDiscount_greater_than_line_total(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 1;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 50000원 할인 쿠폰", Money.wons(50000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money appliedCouponDiscount = item.getAppliedCouponDiscount();
        //then
        assertThat(appliedCouponDiscount).isEqualTo(item.getProductLineTotal());
    }

    @Test
    @DisplayName("주문 상품 총 상품 할인 금액을 반환한다")
    void getDiscountLineTotal(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money discountLineTotal = item.getDiscountLineTotal();
        //then
        assertThat(discountLineTotal).isEqualTo(price.getDiscountAmount().multiple(quantity));
    }

    @Test
    @DisplayName("주문 상품 총 원본 가격 금액을 반환한다")
    void getOriginalLineTotal(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money originalLineTotal = item.getOriginalLineTotal();
        //then
        assertThat(originalLineTotal).isEqualTo(price.getOriginalPrice().multiple(quantity));
    }

    @Test
    @DisplayName("주문 상품 총 판매 금액(쿠폰 할인 금액 적용)을 반환한다")
    void getFinalLineTotal(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);
        //when
        Money finalLineTotal = item.getFinalLineTotal();
        //then
        assertThat(finalLineTotal).isEqualTo(price.getDiscountedPrice().multiple(quantity).subtract(itemCoupon.getDiscountAmount()));
    }

    @Test
    @DisplayName("상품 쿠폰을 변경한다")
    void changeCoupon(){
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "상품 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, itemCoupon, quantity, options);

        OrderCouponSnapshot newCoupon = OrderCouponSnapshot.of(2L, "상품 2000원 할인 쿠폰", Money.wons(2000L));
        //when
        item.changeCoupon(newCoupon);
        //then
        assertThat(item.getItemCoupon()).isEqualTo(newCoupon);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCoupon")
    void hasCoupon(String description, OrderCouponSnapshot coupon, boolean expectedResult) {
        //given
        String sheetItemId = "sheetItemId";
        int quantity = 10;
        ProductSnapshot product = Instancio.create(ProductSnapshot.class);
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = Instancio.ofList(ProductOptionSnapshot.class).size(2).create();
        OrderSheetItem item = OrderSheetItem.create(sheetItemId, product, price, coupon, quantity, options);
        //when
        boolean hasCoupon = item.hasCoupon();
        //then
        assertThat(hasCoupon).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> provideCoupon() {
        return Stream.of(
                Arguments.of(
                        "쿠폰 미적용",
                        OrderCouponSnapshot.empty(),
                        false
                ),
                Arguments.of(
                        "쿠폰 적용",
                        OrderCouponSnapshot.of(1L, "1000원 할인 쿠폰", Money.wons(1000L)),
                        true
                )
        );
    }
}
