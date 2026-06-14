package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderSheetTest {

    private static final OrderCouponSnapshot VALID_CART_COUPON = OrderCouponSnapshot.of(2L, "장바구니 쿠폰", Money.wons(1000L));
    private static final OrderCouponSnapshot VALID_ITEM_COUPON = OrderCouponSnapshot.of(1L, "상품 쿠폰", Money.wons(1000L));

    @Test
    @DisplayName("주문서를 생성한다")
    void create() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(1L, "장바구니 1000원 할인", Money.wons(1000L));
        LocalDateTime createdAt = LocalDateTime.now();
        long ttl = 30;
        OrderSheetItem orderSheetItem = createOrderSheetItem(VALID_ITEM_COUPON);
        //when
        OrderSheet orderSheet = OrderSheet.create("sheetId", orderer, shippingAddress, List.of(orderSheetItem), cartCoupon, createdAt, ttl);
        //then
        assertThat(orderSheet)
                .extracting("sheetId", "orderer", "shippingAddress", "cartCoupon", "totalOriginalPrice", "totalProductDiscountAmount",
                        "totalCouponDiscountAmount", "usedPoints", "totalPaymentAmount", "expiresAt")
                .containsExactlyInAnyOrder(
                        "sheetId", orderer, shippingAddress, cartCoupon,
                        Money.wons(10000L), Money.wons(1000L), Money.wons(2000L), Money.ZERO, Money.wons(7000L),
                        createdAt.plusMinutes(ttl)
                );
    }

    @Test
    @DisplayName("주문서 주문자가 userId와 다르면 예외가 발생한다")
    void validateAccess_not_match_userId() {
        //given
        LocalDateTime now = LocalDateTime.now();
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateAccess(99L, now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
    }

    @Test
    @DisplayName("주문서가 만료되었으면 예외가 발생한다")
    void validateAccess_expired() {
        //given
        LocalDateTime now = LocalDateTime.now();
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateAccess(1L, now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
    }

    @Nested
    @DisplayName("주문서 만료 여부")
    class Expired {

        @Test
        @DisplayName("주문서가 만료되지 않은 경우 false를 반환한다")
        void isExpired_false() {
            //given
            LocalDateTime currentTime = LocalDateTime.of(2026, 1, 1, 12, 20);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            //when
            boolean expired = orderSheet.isExpired(currentTime);
            //then
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("주문서가 만료된 경우 true를 반환한다")
        void isExpired_true(){
            //given
            LocalDateTime currentTime = LocalDateTime.of(2026, 1, 1, 12, 40);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            //when
            boolean expired = orderSheet.isExpired(currentTime);
            //then
            assertThat(expired).isTrue();
        }
    }


    @Test
    @DisplayName("주문서의 남은 만료 시간을 반환한다")
    void getRemainingTtl() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        //when
        Duration remainingTtl = orderSheet.getRemainingTtl(currentTime);
        //then
        assertThat(remainingTtl).isEqualTo(Duration.between(currentTime, orderSheet.getExpiresAt()));
    }

    @Test
    @DisplayName("배송 정보를 수정한다")
    void changeShippingAddress() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-9876-4321", "12345",
                "서울시 테헤란로 123", "123동 1234호");
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        //when
        orderSheet.changeShippingAddress(shippingAddress);
        //then
        assertThat(orderSheet.getShippingAddress()).isEqualTo(shippingAddress);
    }

    @Test
    @DisplayName("포인트를 변경한다")
    void changeUsedPoints(){
        //given
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        Money ownedPoints = Money.wons(10000L);
        Money usedPoints = Money.wons(500L);
        PointUsagePolicy policy = (eligibleAmount) -> Money.wons(1000L);
        //when
        orderSheet.changeUsedPoints(usedPoints, ownedPoints, policy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(500L));
    }

    @Test
    @DisplayName("사용하려는 포인트가 주문 사용 가능 포인트를 초과하는 경우 예외가 발생한다")
    void changeUsedPoints_point_policy_violation(){
        //given
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        Money ownedPoints = Money.wons(10000L);
        Money usedPoints = Money.wons(2000L);
        PointUsagePolicy policy = (eligibleAmount) -> Money.wons(1000L);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.changeUsedPoints(usedPoints, ownedPoints, policy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_POINT_POLICY_VIOLATION);
    }

    @Test
    @DisplayName("주문 상품을 반환한다")
    void getItem(){
        //given
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        //when
        OrderSheetItem sheetItem = orderSheet.getItem("sheetItemId");
        //then
        assertThat(sheetItem)
                .extracting("sheetItemId", "quantity")
                .containsExactlyInAnyOrder("sheetItemId", 1);
    }

    @Test
    @DisplayName("주문 상품을 찾을 수 없으면 예외가 발생한다")
    void getItem_notFound(){
        //given
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.getItem("notFound"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @ParameterizedTest(name = "보유 포인트 {0}원, 정책 한도 {1}원일 때, 적용 가능 포인트는 {2}원이다")
    @CsvSource({
            "10000, 1000, 1000",
            "500,   1000, 500",
            "1000,  1000, 1000"
    })
    void calcAvailablePoints(long owned, long limit, long expected){
        //given
        OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
        Money ownedPoints = Money.wons(owned);
        PointUsagePolicy policy = (eligibleAmount) -> Money.wons(limit);
        //when
        Money availablePoints = orderSheet.calcAvailablePoints(ownedPoints, policy);
        //then
        assertThat(availablePoints).isEqualTo(Money.wons(expected));
    }

    @Nested
    @DisplayName("상품 쿠폰 변경시 포인트 재계산")
    class ChangeItemCouponRecalculateTotals {

        @Test
        @DisplayName("포인트를 사용하지 않은 경우 포인트는 0원으로 유지된다")
        void changeItemCoupon_no_point_used(){
            //given
            String sheetItemId = "sheetItemId";
            Money ownedPoints = Money.wons(10000L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            PointUsagePolicy policy = (eligibleAmount) -> Money.wons(1000L);
            OrderCouponSnapshot newItemCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeItemCoupon(sheetItemId, newItemCoupon, ownedPoints, policy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.ZERO);
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(5000L));
            assertThat(orderSheet.getItem(sheetItemId).getItemCoupon())
                    .isEqualTo(newItemCoupon);
        }

        @Test
        @DisplayName("상품 쿠폰을 변경할때 적용되어있는 포인트가 사용 가능 포인트 이내라면 적용 포인트는 그대로 유지된다")
        void changeItemCoupon_points_maintain(){
            //given
            String sheetItemId = "sheetItemId";
            Money ownedPoints = Money.wons(10000L);
            PointUsagePolicy oldPolicy = (eligibleAmount) -> Money.wons(2000L);
            PointUsagePolicy newPolicy = (eligibleAmount) -> Money.wons(1500L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            orderSheet.changeUsedPoints(Money.wons(1000L), Money.wons(10000L), oldPolicy);
            OrderCouponSnapshot newItemCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeItemCoupon(sheetItemId, newItemCoupon, ownedPoints, newPolicy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(1000L));
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getItem(sheetItemId).getItemCoupon())
                    .isEqualTo(newItemCoupon);
        }

        @Test
        @DisplayName("상품 쿠폰을 변경했을때 적용되어있는 포인트가 새 한도를 초과하는 경우 사용 포인트가 조정된다")
        void changeItemCoupon_points_adjusted(){
            //given
            String sheetItemId = "sheetItemId";
            Money ownedPoints = Money.wons(10000L);
            PointUsagePolicy oldPolicy = (eligibleAmount) -> Money.wons(2000L);
            PointUsagePolicy newPolicy = (eligibleAmount) -> Money.wons(500L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            orderSheet.changeUsedPoints(Money.wons(1000L), Money.wons(10000L), oldPolicy);
            OrderCouponSnapshot newItemCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeItemCoupon(sheetItemId, newItemCoupon, ownedPoints, newPolicy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(500L));
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(4500L));
            assertThat(orderSheet.getItem(sheetItemId).getItemCoupon())
                    .isEqualTo(newItemCoupon);
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class ChangeCartCouponRecalculateTotals {

        @Test
        @DisplayName("포인트를 사용하지 않은 경우 포인트는 0원으로 유지된다")
        void changeCartCoupon_no_point_used(){
            //given
            Money ownedPoints = Money.wons(10000L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            PointUsagePolicy policy = (eligibleAmount) -> Money.wons(1000L);
            OrderCouponSnapshot newCartCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeCartCoupon(newCartCoupon, ownedPoints, policy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.ZERO);
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(5000L));
            assertThat(orderSheet.getCartCoupon()).isEqualTo(newCartCoupon);
        }

        @Test
        @DisplayName("장바구니 쿠폰을 변경할때 적용되어있는 포인트가 사용 가능 포인트 이내라면 적용 포인트는 그대로 유지된다")
        void changeCartCoupon_points_maintain(){
            //given
            Money ownedPoints = Money.wons(10000L);
            PointUsagePolicy oldPolicy = (eligibleAmount) -> Money.wons(2000L);
            PointUsagePolicy newPolicy = (eligibleAmount) -> Money.wons(1500L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            orderSheet.changeUsedPoints(Money.wons(1000L), Money.wons(10000L), oldPolicy);
            OrderCouponSnapshot newCartCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeCartCoupon(newCartCoupon, ownedPoints, newPolicy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(1000L));
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getCartCoupon()).isEqualTo(newCartCoupon);
        }

        @Test
        @DisplayName("장바구니 쿠폰을 변경했을때 적용되어있는 포인트가 새 한도를 초과하는 경우 사용 포인트가 조정된다")
        void changeCartCoupon_points_adjusted(){
            //given
            Money ownedPoints = Money.wons(10000L);
            PointUsagePolicy oldPolicy = (eligibleAmount) -> Money.wons(2000L);
            PointUsagePolicy newPolicy = (eligibleAmount) -> Money.wons(500L);
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            orderSheet.changeUsedPoints(Money.wons(1000L), Money.wons(10000L), oldPolicy);
            OrderCouponSnapshot newCartCoupon = OrderCouponSnapshot.of(10L, "3000원 할인 쿠폰", Money.wons(3000L));
            //when
            orderSheet.changeCartCoupon(newCartCoupon, ownedPoints, newPolicy);
            //then
            assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(500L));
            assertThat(orderSheet.getTotalCouponDiscountAmount()).isEqualTo(Money.wons(4000L));
            assertThat(orderSheet.getTotalPaymentAmount()).isEqualTo(Money.wons(4500L));
            assertThat(orderSheet.getCartCoupon()).isEqualTo(newCartCoupon);
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 적용 여부")
    class HasCartCoupon {
        @Test
        @DisplayName("장바구니 쿠폰이 적용되어 있으면 true를 반환한다")
        void hasCartCoupon_true(){
            //given
            OrderSheet orderSheet = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            //when
            boolean hasCartCoupon = orderSheet.hasCartCoupon();
            //then
            assertThat(hasCartCoupon).isTrue();
        }

        @Test
        @DisplayName("장바구니 쿠폰이 적용되어있지 않으면 false를 반환한다")
        void hasCartCoupon_false(){
            //given
            OrderSheet orderSheet = createOrderSheet(OrderCouponSnapshot.empty(), VALID_ITEM_COUPON);
            //when
            boolean hasCartCoupon = orderSheet.hasCartCoupon();
            //then
            assertThat(hasCartCoupon).isFalse();
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 적용 여부")
    class HasItemCoupon {

        @Test
        @DisplayName("상품 쿠폰이 적용되어있는 상품이 있다면 true를 반환한다")
        void hasItemCoupon_true(){
            //given
            OrderSheet orderSheetItem = createOrderSheet(VALID_CART_COUPON, VALID_ITEM_COUPON);
            //when
            boolean hasItemCoupon = orderSheetItem.hasItemCoupon();
            //then
            assertThat(hasItemCoupon).isTrue();
        }

        @Test
        @DisplayName("상품 쿠폰이 적용되어있는 상품이 있다면 true를 반환한다")
        void hasItemCoupon_false(){
            //given
            OrderSheet orderSheetItem = createOrderSheet(VALID_CART_COUPON, OrderCouponSnapshot.empty());
            //when
            boolean hasItemCoupon = orderSheetItem.hasItemCoupon();
            //then
            assertThat(hasItemCoupon).isFalse();
        }
    }

    private OrderSheet createOrderSheet(OrderCouponSnapshot cartCoupon, OrderCouponSnapshot itemCoupon) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderSheetItem sheetItem = createOrderSheetItem(itemCoupon);
        LocalDateTime fixedTime = LocalDateTime.of(2026, 1, 1, 12, 0);
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, fixedTime, 30);
    }

    private OrderSheetItem createOrderSheetItem(OrderCouponSnapshot itemCoupon) {
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = List.of(ProductOptionSnapshot.of("사이즈", "XL"));
        return OrderSheetItem.create("sheetItemId", product, price, itemCoupon, 1, options);
    }

}
