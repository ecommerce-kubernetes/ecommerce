package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderValidatorTest {

    private final OrderValidator orderValidator = new OrderValidator();
    private final OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    private final PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(properties);

    @Test
    @DisplayName("주문서의 주문 상품 가격과 상품 결과의 판매가격이 다르면 예외가 발생한다")
    void validateOrderProduct() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        List<OrderProductResult.Info> invalidProducts = orderSheet.getItems().stream()
                .map(item -> fixtureMonkey.giveMeBuilder(OrderProductResult.Info.class)
                        .set("productSnapshot.productVariantId", item.getProductVariantId())
                        .set("priceSnapshot.discountedPrice", item.getDiscountedPrice().add(Money.wons(100L)))
                        .sample()
                ).toList();

        OrderProductResult.ProductList productResult = fixtureMonkey.giveMeBuilder(OrderProductResult.ProductList.class)
                .set("products", invalidProducts)
                .sample();
        OrderCouponResult.Calculate couponResult = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productResult, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.PRODUCT_PRICE_CHANGE);
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 가격이 다른 경우 예외가 발생한다")
    void validateOrderCartCoupon() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        OrderProductResult.ProductList productResult = createValidProductList(orderSheet);
        OrderCouponResult.Calculate couponResult = fixtureMonkey.giveMeBuilder(OrderCouponResult.Calculate.class)
                .set("cartCoupon.discountAmount", orderSheet.getCartCoupon().getDiscountAmount().add(Money.wons(500L)))
                .sample();
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productResult, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.CART_COUPON_DISCOUNT_CHANGE);
    }

    @Test
    @DisplayName("상품 쿠폰 할인 가격이 다른 경우 예외가 발생한다")
    void validateOrderItemCoupon() {
        OrderSheet orderSheet = createOrderSheet();
        OrderProductResult.ProductList productResult = createValidProductList(orderSheet);
        List<OrderCouponResult.ItemCoupon> invalidItemCoupons = orderSheet.getItems().stream()
                .map(item -> fixtureMonkey.giveMeBuilder(OrderCouponResult.ItemCoupon.class)
                        .set("productVariantId", item.getProductVariantId())
                        .set("itemCoupon.discountAmount", item.getAppliedCouponDiscount().add(Money.wons(100L)))
                        .sample()
                ).toList();

        OrderCouponResult.Calculate couponResult = fixtureMonkey.giveMeBuilder(OrderCouponResult.Calculate.class)
                .set("cartCoupon.discountAmount", orderSheet.getCartCoupon().getDiscountAmount())
                .set("itemCoupons", invalidItemCoupons)
                .sample();
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productResult, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ITEM_COUPON_DISCOUNT_CHANGE);
    }

    @Test
    @DisplayName("주문서 포인트가 사용 가능 포인트를 넘는다면 예외가 발생한다")
    void validateOrderPoints() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        orderSheet.changeUsedPoints(Money.wons(1000L), Money.wons(10000L));
        OrderProductResult.ProductList productList = createValidProductList(orderSheet);
        OrderCouponResult.Calculate calculate = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint pointResult = fixtureMonkey.giveMeBuilder(OrderUserResult.UserPoint.class)
                .set("ownedPoints", Money.wons(500L))
                .sample();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, calculate, pointResult, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.POINTS_DISCOUNT_CHANGE);
    }

    private OrderProductResult.ProductList createValidProductList(OrderSheet orderSheet) {
        List<OrderProductResult.Info> validProducts = orderSheet.getItems().stream()
                .map(item -> fixtureMonkey.giveMeBuilder(OrderProductResult.Info.class)
                        .set("productSnapshot.productVariantId", item.getProductVariantId())
                        .set("priceSnapshot.discountedPrice", item.getDiscountedPrice())
                        .sample()
                ).toList();

        return fixtureMonkey.giveMeBuilder(OrderProductResult.ProductList.class)
                .set("products", validProducts)
                .sample();
    }

    private OrderCouponResult.Calculate createValidCouponResult(OrderSheet orderSheet) {
        List<OrderCouponResult.ItemCoupon> validItemCoupons = orderSheet.getItems().stream()
                .map(item -> fixtureMonkey.giveMeBuilder(OrderCouponResult.ItemCoupon.class)
                        .set("productVariantId", item.getProductVariantId())
                        .set("itemCoupon.discountAmount", item.getAppliedCouponDiscount())
                        .sample())
                .toList();

        return fixtureMonkey.giveMeBuilder(OrderCouponResult.Calculate.class)
                .set("cartCoupon.discountAmount", orderSheet.getCartCoupon().getDiscountAmount())
                .set("itemCoupons", validItemCoupons)
                .sample();
    }

    private OrderUserResult.UserPoint createValidUserPoint(OrderSheet orderSheet) {
        return fixtureMonkey.giveMeBuilder(OrderUserResult.UserPoint.class)
                .set("ownedPoints", orderSheet.getUsedPoints().add(Money.wons(10000L)))
                .sample();
    }

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create("sheetItemId", product, price, itemCoupon, 1, options);
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, LocalDateTime.now(), 30);
    }
}
