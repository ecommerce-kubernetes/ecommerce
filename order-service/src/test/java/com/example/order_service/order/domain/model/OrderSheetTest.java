package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
    @DisplayName("주문서의 배송 정보를 변경할때 배송 정보가 없으면 예외가 발생한다.")
    void changeShippingAddress_shippingAddress_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.changeShippingAddress(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경할 배송 정보는 필수 입니다.");
    }

    @Test
    @DisplayName("주문 항목에 상품 쿠폰을 적용한다.")
    void applyItemCoupon() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", policy, 1);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //then
        assertThat(item.getItemCouponSnapshot()).isEqualTo(itemCoupon);
    }
    
    @Test
    @DisplayName("주문 항목 상품 쿠폰 적용으로 인해 적용된 포인트가 적용 가능 포인트를 초과하는 경우 적용 가능 포인트를 한도로 적용 포인트가 보정된다.")
    void applyItemCoupon_usedPoints_exceed_availablePoints() {
        //given
        Money usedPoints = Money.wons(2700L);
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        orderSheet.applyPoints(usedPoints, pointPolicy);

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(2000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "2000원 할인 쿠폰", couponPolicy, 1);
        //when
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(2500L));
    }

    @Test
    @DisplayName("상품 쿠폰을 적용할 때 주문 상품을 찾을 수 없으면 예외가 발생한다.")
    void applyItemCoupon_notFound_orderSheetItem() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", couponPolicy, 1);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyItemCoupon("unknown", itemCoupon, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용한다")
    void applyCartCoupon(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, Money.wons(5000L));

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getCartCoupon()).isEqualTo(cartCoupon);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용할때 장바구니 쿠폰이 없으면 예외가 발생한다")
    void applyCartCoupon_coupon_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyCartCoupon(null, pointPolicy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용할 쿠폰 정보는 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용할때 최소 결제 금액을 만족하지 못하면 예외가 발생한다.")
    void applyCartCoupon_not_satisfy_minimumPaymentAmount(){
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, Money.wons(50000L));

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyCartCoupon(cartCoupon, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.CART_COUPON_MINIMUM_PAYMENT_NOT_MET);
    }

    @Test
    @DisplayName("장바구니 쿠폰 적용으로 인해 적용된 포인트가 적용 가능 포인트를 초과하는 경우 적용 가능 포인트를 한도로 적용 포인트가 보정된다.")
    void applyCartCoupon_usedPoints_exceed_availablePoints(){
        //given
        Money usedPoints = Money.wons(2700L);
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);

        orderSheet.applyPoints(usedPoints, pointPolicy);

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(5000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "5000원 할인 쿠폰", policy, Money.wons(10000L));
        //when
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(2200L));
    }

    @Test
    @DisplayName("포인트를 적용한다.")
    void applyPoints(){
        //given
        Money usedPoints = Money.wons(1000L);
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        DefaultPointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        orderSheet.applyPoints(usedPoints, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("포인트를 적용할때 적용 가능 포인트를 초과하는 경우 예외가 발생한다.")
    void applyPoints_exceed_availablePoints(){
        //given
        Money usedPoints = Money.wons(2800L);
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        OrderSheetItem item = createOrderSheetItem();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(item), expiresAt);

        OrderSheetProperties properties = new OrderSheetProperties(30, BigDecimal.valueOf(0.1));
        DefaultPointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(properties);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyPoints(usedPoints, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
    }

    private OrderSheetItem createOrderSheetItem() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, Collections.emptyList());
    }
}
