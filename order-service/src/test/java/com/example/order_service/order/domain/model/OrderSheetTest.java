package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderSheetTest {

    @Test
    @DisplayName("주문서를 생성하면 주문 항목을 토대로 가격이 계산된다.")
    void create() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);
        //then
        assertThat(orderSheet.getOrderer()).isEqualTo(orderer);
        assertThat(orderSheet.getItems()).hasSize(1)
                .containsExactly(item);

        assertThat(orderSheet.getExpiresAt()).isEqualTo(expiresAt);

        assertThat(orderSheet)
                .extracting(OrderSheet::getTotalOriginalPrice, OrderSheet::getTotalProductDiscountAmount,
                        OrderSheet::getTotalCouponDiscountAmount, OrderSheet::getUsedPoints, OrderSheet::getTotalPaymentAmount)
                .containsExactly(
                        item.getOriginalLineTotal(), item.getProductDiscountLineTotal(),
                        Money.ZERO, Money.ZERO, item.getFinalAmount()
                );
    }

    @Test
    @DisplayName("주문서를 생성할때 주문자 정보가 없으면 예외가 발생한다.")
    void create_orderer_null() {
        //given
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(null, List.of(item), expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 주문자는 필수이다.");
    }

    @Test
    @DisplayName("주문서를 생성할때 주문 항목이 0개 이하인 경우 예외가 발생한다.")
    void create_items_empty() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        List<OrderSheetItem> items = Collections.emptyList();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(orderer, items, expiresAt))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }

    @Test
    @DisplayName("주문서를 생성할때 주문서 만료 시간이 없으면 예외가 발생한다.")
    void create_expiresAt_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(orderer, List.of(item), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 만료 시간은 필수이다.");
    }

    @Test
    @DisplayName("주문서의 배송 정보를 변경한다")
    void changeShippingAddress() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
        //when
        orderSheet.changeShippingAddress(shippingAddress);
        //then
        assertThat(orderSheet.getShippingAddress()).isEqualTo(shippingAddress);
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰을 적용하면 가격 정보가 다시 계산된다.")
    void applyItemCoupon() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", Money.wons(1000L));
        //when
        orderSheet.applyItemCoupon(item.getId(), itemCoupon);
        //then
        assertThat(orderSheet)
                .extracting(OrderSheet::getTotalCouponDiscountAmount, OrderSheet::getTotalPaymentAmount)
                .containsExactly(
                        itemCoupon.getDiscountAmount(), Money.wons(26000L)
                );
    }

    @Test
    @DisplayName("상품 쿠폰을 적용할 때 주문 상품을 찾을 수 없으면 예외가 발생한다.")
    void applyItemCoupon_notFound_orderSheetItem() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", Money.wons(1000L));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyItemCoupon("unknown", itemCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 쿠폰을 적용할때 쿠폰 할인 금액이 주문 항목의 판매가 총액을 초과하면 예외가 발생한다.")
    void applyItemCoupon_exceed_itemLineTotal(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 30000원 할인", Money.wons(30000L));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyItemCoupon(item.getId(), itemCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ITEM_COUPON);
    }

    @Test
    @DisplayName("장바구니 쿠폰이 적용되어있는 주문서의 주문 항목에 상품 쿠폰을 적용하는 경우 상품 쿠폰 적용으로 인해 장바구니 쿠폰 할인금액이 총 상품 최종금액을 넘어서면 예외가 발생한다.")
    void applyItemCoupon_conflict_coupon_combine(){
        //given
        //when
        //then
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용하면 가격 정보가 다시 계산된다")
    void applyCartCoupon(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "10000원 할인 쿠폰", Money.wons(10000L));
        //when
        orderSheet.applyCartCoupon(cartCoupon);
        //then
        assertThat(orderSheet.getCartCoupon()).isEqualTo(cartCoupon);
        assertThat(orderSheet)
                .extracting(OrderSheet::getTotalCouponDiscountAmount, OrderSheet::getTotalPaymentAmount)
                .containsExactly(
                        cartCoupon.getDiscountAmount(), Money.wons(17000L)
                );
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 금액이 총 주문 항목 최종 금액을 초과하면 예외가 발생한다.")
    void applyCartCoupon_exceed_TotalItemFinalAmount(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "30000원 할인 쿠폰", Money.wons(30000L));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyCartCoupon(cartCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_CART_COUPON);
    }

    private OrderSheetItem createOrderSheetItem() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
    }
}
