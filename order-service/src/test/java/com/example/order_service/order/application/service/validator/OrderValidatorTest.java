package com.example.order_service.order.application.service.validator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.application.service.fixture.OrderCartResultFixture;
import com.example.order_service.order.application.service.fixture.OrderCouponResultFixture;
import com.example.order_service.order.application.service.fixture.OrderProductResultFixture;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
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

        OrderCartItemsResult.Item item = OrderCartResultFixture.anOrderCartItem().cartItemId(1L)
                .build();

        OrderCartItemsResult cartItems = OrderCartResultFixture.anOrderCartItems()
                .items(List.of(item))
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateMissingCartItems(requestCartItemIds, cartItems))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("누락된 상품이 있는지 검증한다")
    void validateOrderable_whenMissingProduct_thenThrownException() {
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
    void validateOrderable_whenStatusOnSale_thenThrownException() {
        //given
        OrderProductsResult.OrderProductDetail product = OrderProductResultFixture.anOrderProduct()
                .status(OrderProductStatus.STOP_SALE)
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateOrderable(product, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
    }

    @Test
    @DisplayName("상품 재고가 부족한지 검증한다")
    void validateOrderable_whenInsufficientStock_thenThrownException() {
        //given
        OrderProductsResult.OrderProductDetail product = OrderProductResultFixture.anOrderProduct()
                .stock(10)
                .build();
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
    void validateItemCoupon_whenCouponIsNull_thenThrownException() {
        ItemCouponsResult.ItemCouponResult couponResult = null;
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(couponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 쿠폰이 사용 가능한지 확인한다.")
    void validateItemCoupon_whenCouponUnAvailable_thenThrownException() {
        //given
        ItemCouponsResult.ItemCouponResult itemCouponResult = OrderCouponResultFixture.anItemCoupon()
                .status(OrderCouponStatus.USED)
                .build();

        LocalDateTime currentTime = LocalDateTime.now();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(itemCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_UNAVAILABLE);
    }

    @Test
    @DisplayName("상품 쿠폰이 만료되었는지 검증한다.")
    void validateItemCoupon_whenCouponExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now().minusDays(1);
        ItemCouponsResult.ItemCouponResult itemCouponResult = OrderCouponResultFixture.anItemCoupon()
                .expiresAt(expiresAt)
                .build();
        LocalDateTime currentTime = LocalDateTime.now();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateItemCoupon(itemCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_EXPIRED);
    }
    
    @Test
    @DisplayName("장바구니 쿠폰이 누락되었는지 검증한다.")
    void validateCartCoupon_whenCouponIsNull_thenThrownException() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        CartCouponResult cartCouponResult = null;
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateCartCoupon(cartCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 쿠폰이 사용 가능한지 검증한다.")
    void validateCartCoupon_whenCouponUnavailable_thenThrownException() {
        //given
        CartCouponResult cartCouponResult = OrderCouponResultFixture.anCartCoupon()
                .status(OrderCouponStatus.USED)
                .build();
        LocalDateTime currentTime = LocalDateTime.now();

        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateCartCoupon(cartCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_UNAVAILABLE);
    }

    @Test
    @DisplayName("장바구니 쿠폰이 만료 되었는지 검증한다.")
    void validateCartCoupon_whenCouponExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now().minusDays(1);
        LocalDateTime currentTime = LocalDateTime.now();

        CartCouponResult cartCouponResult = OrderCouponResultFixture.anCartCoupon()
                .expiresAt(expiresAt)
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validateCartCoupon(cartCouponResult, currentTime))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_COUPON_EXPIRED);
    }
}