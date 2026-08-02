package com.example.order_service.order.application.service.validator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

class OrderValidatorTest {

    private final OrderValidator orderValidator = new OrderValidator();

    @Test
    @DisplayName("누락된 장바구니 항목이 있는지 검증한다")
    void validateMissingCartItems() {
        //given
        List<Long> requestCartItemIds = List.of(1L, 2L);

        OrderCartItemsResult.Item item = OrderCartItemsResult.Item.builder()
                .cartItemId(1L)
                .productVariantId(1L)
                .quantity(3)
                .build();

        OrderCartItemsResult cartItems = OrderCartItemsResult.builder().items(List.of(item)).build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateMissingCartItems(requestCartItemIds, cartItems))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("누락된 상품이 있는지 검증한다")
    void validateOrderable_missing_product() {
        //given
        OrderProductsResult.OrderProductDetail product = null;
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateOrderable(product, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 불가능한 상품이 있는지 검증한다")
    void validateOrderable_product_unOrderable() {
        //given
        OrderProductsResult.OrderProductDetail product = Instancio.of(OrderProductsResult.OrderProductDetail.class)
                .set(field("status"), OrderProductStatus.STOP_SALE)
                .set(field("stock"), 100)
                .create();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateOrderable(product, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
    }

    @Test
    @DisplayName("상품 재고가 부족한지 검증한다")
    void validateOrderable_product_insufficient_stock() {
        //given
        OrderProductsResult.OrderProductDetail product = Instancio.of(OrderProductsResult.OrderProductDetail.class)
                .set(field("status"), OrderProductStatus.ON_SALE)
                .set(field("stock"), 10)
                .create();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateOrderable(product, 15))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("포인트가 충분한지 검증한다")
    void validateAvailablePoints() {
        //given
        Money availablePoints = Money.wons(1000L);
        Money usedPoints = Money.wons(2000L);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateAvailablePoints(availablePoints, usedPoints))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
    }

    @Test
    @DisplayName("누락된 상품 쿠폰이 있는지 검증한다.")
    void validateItemCoupon_missing_coupon() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(null, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 쿠폰이 사용 가능한지 확인한다.")
    void validateItemCoupon_coupon_unavailable() {
        //given
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", couponDiscountPolicy, 1);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(10);
        LocalDateTime currentTime = LocalDateTime.now();

        ItemCouponsResult.ItemCouponResult itemCouponResult = ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.USED)
                .itemCoupon(itemCoupon)
                .expiresAt(expiresAt)
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(itemCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_UNAVAILABLE);
    }

    @Test
    @DisplayName("쿠폰이 만료되었는지 검증한다.")
    void validateItemCoupon_coupon_expired() {
        //given
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", couponDiscountPolicy, 1);
        LocalDateTime expiresAt = LocalDateTime.now().minusDays(1);
        LocalDateTime currentTime = LocalDateTime.now();

        ItemCouponsResult.ItemCouponResult itemCouponResult = ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .itemCoupon(itemCoupon)
                .expiresAt(expiresAt)
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(itemCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_EXPIRED);
    }
}