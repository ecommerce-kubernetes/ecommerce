package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class OrderSheetTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문서를 생성한다")
    void create() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        CreateOrderSheetItemContext itemCtx = createOrderSheetItemContext(1L);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(orderer)
                .items(List.of(itemCtx))
                .expiresAt(expiresAt)
                .build();
        //when
        OrderSheet orderSheet = OrderSheet.create(context, idGenerator);
        //then
        assertThat(orderSheet.getOrderer()).isEqualTo(orderer);
        assertThat(orderSheet.getItems()).hasSize(1);
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.ZERO);
        assertThat(orderSheet.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("주문서를 생성할때 주문 항목이 0개 이하인 경우 예외가 발생한다.")
    void create_whenItemsEmpty_thenThrownException() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(orderer)
                .items(Collections.emptyList())
                .expiresAt(expiresAt)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
    }

    @Test
    @DisplayName("주문서를 생성할때 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_whenIdGeneratorIsNull_thenThrownException() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        CreateOrderSheetItemContext itemCtx = createOrderSheetItemContext(1L);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(orderer)
                .items(List.of(itemCtx))
                .expiresAt(expiresAt)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSheet.create(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문서(OrderSheet) 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문서의 배송 정보를 변경한다")
    void changeShippingAddress() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호");
        //when
        orderSheet.changeShippingAddress(shippingAddress);
        //then
        assertThat(orderSheet.getShippingAddress()).isEqualTo(shippingAddress);
    }

    @Test
    @DisplayName("주문서의 배송 정보를 변경할때 배송 정보가 없으면 예외가 발생한다.")
    void changeShippingAddress_whenShippingAddressIsNull_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
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
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        OrderSheetItem item = orderSheet.getItems().getFirst();

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", policy, 1);

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //then
        assertThat(item.getItemCouponSnapshot()).isEqualTo(itemCoupon);
    }
    
    @Test
    @DisplayName("상품 쿠폰 적용으로 포인트 사용 한도를 초과하면 사용 포인트가 사용 한도까지 보정된다.")
    void applyItemCoupon_whenUsedPointsExceedUsageLimit_thenAdjustUsedPoints() {
        //given
        Money usedPoints = Money.wons(2600L);
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withUsedPoint(usedPoints, pointPolicy).build();

        OrderSheetItem item = orderSheet.getItems().getFirst();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(2000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "2000원 할인 쿠폰", couponPolicy, 1);
        //when
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(2500L));
    }

    @Test
    @DisplayName("상품 쿠폰을 적용할 때 주문 상품을 찾을 수 없으면 예외가 발생한다.")
    void applyItemCoupon_whenOrderSheetItemNotFound_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", couponPolicy, 1);

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyItemCoupon(999L, itemCoupon, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_ITEM_NOT_FOUND);
    }
    
    @Test
    @DisplayName("동일한 상품 쿠폰이 다른 주문 항목에 이미 적용되어 있으면 예외가 발생한다.")
    void applyItemCoupon_whenItemCouponAlreadyApplied_thenThrowException() {
        //given
        CreateOrderSheetItemContext itemCtx1 = createOrderSheetItemContext(1L);
        CreateOrderSheetItemContext itemCtx2 = createOrderSheetItemContext(2L);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withItemContexts(itemCtx1, itemCtx2)
                .build();

        OrderSheetItem item1 = orderSheet.getItems().getFirst();
        OrderSheetItem item2 = orderSheet.getItems().getLast();

        CouponDiscountPolicy couponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", couponPolicy, 1);

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));

        orderSheet.applyItemCoupon(item1.getId(), itemCoupon, pointPolicy);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyItemCoupon(item2.getId(), itemCoupon, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.DUPLICATE_ITEM_COUPON_APPLICATION);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용한다")
    void applyCartCoupon(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, Money.wons(5000L));

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getCartCoupon()).isEqualTo(cartCoupon);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용할때 장바구니 쿠폰이 없으면 예외가 발생한다")
    void applyCartCoupon_whenCartCouponIsNull_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyCartCoupon(null, pointPolicy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용할 쿠폰 정보는 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용할때 최소 결제 금액을 만족하지 못하면 예외가 발생한다.")
    void applyCartCoupon_whenMinimumPaymentAmountNotMet_thenThrowException(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, Money.wons(50000L));

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyCartCoupon(cartCoupon, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.CART_COUPON_MINIMUM_PAYMENT_NOT_MET);
    }

    @Test
    @DisplayName("장바구니 쿠폰 적용으로 인해 포인트 사용 한도를 초과하면 사용 포인트가 사용 한도까지 보정된다.")
    void applyCartCoupon_whenUsedPointsExceedUsageLimit_thenAdjustUsedPoints(){
        //given
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        Money usedPoints = Money.wons(2700L);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withUsedPoint(usedPoints, pointPolicy).build();

        CouponDiscountPolicy cartCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(5000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "5000원 할인 쿠폰", cartCouponPolicy, Money.wons(10000L));
        //when
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(2200L));
    }

    @Test
    @DisplayName("포인트를 적용한다.")
    void applyPoints(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        Money usedPoints = Money.wons(1000L);
        DefaultPointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        orderSheet.applyPoints(usedPoints, pointPolicy);
        //then
        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("포인트를 적용할때 적용 가능 포인트를 초과하는 경우 예외가 발생한다.")
    void applyPoints_whenExceedAvailablePoints_thenThrownException(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        Money usedPoints = Money.wons(2800L);
        DefaultPointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.applyPoints(usedPoints, pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
    }

    @Test
    @DisplayName("주문서가 만료되었는지 여부를 반환한다.")
    void isExpired() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now();
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();

        LocalDateTime expiredAt = expiresAt.plusMinutes(10);
        //when
        boolean isExpired = orderSheet.isExpired(expiredAt);
        //then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("주문서의 최대 적용 가능 포인트를 계산한다.")
    void calculateMaxUsablePoints(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        DefaultPointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        Money maxUsablePoints = orderSheet.calculateMaxUsablePoints(pointPolicy);
        //then
        assertThat(maxUsablePoints).isEqualTo(Money.wons(2700L));
    }

    @Test
    @DisplayName("전체 주문 항목 상품 원 가격 총액을 계산한다.")
    void calculateTotalOriginalAmount(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        //when
        Money totalOriginalAmount = orderSheet.calculateTotalOriginalAmount();
        //then
        assertThat(totalOriginalAmount).isEqualTo(Money.wons(30000L));
    }

    @Test
    @DisplayName("전체 주문 항목 상품 할인 금액 총액을 계산한다.")
    void calculateTotalItemDiscount(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        //when
        Money totalItemDiscount = orderSheet.calculateTotalItemDiscount();
        //then
        assertThat(totalItemDiscount).isEqualTo(Money.wons(3000L));
    }

    @Test
    @DisplayName("전체 주문 항목 상품 쿠폰 할인 금액 총액을 계산한다.")
    void calculateTotalItemCouponDiscount(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        OrderSheetItem item = orderSheet.getItems().getFirst();

        CouponDiscountPolicy itemCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", itemCouponPolicy, 1);
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //when
        Money totalItemCouponDiscount = orderSheet.calculateTotalItemCouponDiscount();
        //then
        assertThat(totalItemCouponDiscount).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 금액을 계산한다.")
    void calculateCartCouponDiscount() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy cartCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(5000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "5000원 할인 쿠폰", cartCouponPolicy, Money.wons(10000L));
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //when
        Money cartCouponDiscount = orderSheet.calculateCartCouponDiscount();
        //then
        assertThat(cartCouponDiscount).isEqualTo(Money.wons(5000L));
    }
    
    @Test
    @DisplayName("장바구니 쿠폰 할인 금액이 주문 항목 최종 가격 총액을 초과하면 주문 항목 최종 가격 총액만큼 할인한다.")
    void calculateCartCouponDiscount_whenDiscountExceedsTotalItemsFinalAmount_thenReturnTotalItemsFinalAmount() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy cartCouponPolicy = new FixedCouponDiscountPolicy(Money.wons(50000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "50000원 할인 쿠폰", cartCouponPolicy, Money.wons(10000L));
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);
        //when
        Money cartCouponDiscount = orderSheet.calculateCartCouponDiscount();
        //then
        assertThat(cartCouponDiscount).isEqualTo(Money.wons(27000L));
    }

    @Test
    @DisplayName("총 결제 금액을 계산한다.")
    void calculateTotalPaymentAmount(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        //when
        Money paymentAmount = orderSheet.calculateTotalPaymentAmount();
        //then
        assertThat(paymentAmount).isEqualTo(Money.wons(27000L));
    }

    @Test
    @DisplayName("상품 쿠폰, 장바구니 쿠폰, 포인트가 적용되었을 때 총 결제 금액을 계산한다.")
    void calculateTotalPaymentAmount_whenCouponsAndPointsApplied_thenReturnDiscountedAmount() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));

        OrderSheetItem item = orderSheet.getItems().getFirst();
        CouponDiscountPolicy itemPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "상품 1000원 할인", itemPolicy, 1);
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);

        CouponDiscountPolicy cartPolicy = new FixedCouponDiscountPolicy(Money.wons(5000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 5000원 할인", cartPolicy, Money.wons(10000L));
        orderSheet.applyCartCoupon(cartCoupon, pointPolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointPolicy);
        //when
        Money paymentAmount = orderSheet.calculateTotalPaymentAmount();
        //then
        assertThat(paymentAmount).isEqualTo(Money.wons(20000L));
    }

    @Test
    @DisplayName("잔여 만료시간을 계산한다.")
    void calculateRemainingTtl() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now();
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withExpiresAt(expiresAt)
                .build();

        LocalDateTime currentTime = expiresAt.minusMinutes(10);
        //when
        Duration duration = orderSheet.calculateRemainingTtl(currentTime);
        //then
        assertThat(duration.toMinutes()).isEqualTo(10);
    }

    @Test
    @DisplayName("주문서가 만료된 경우 잔여 만료시간은 0이다")
    void calculateRemainingTtl_whenExpired_thenReturnZeroDuration() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now();
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();

        LocalDateTime currentTime = expiresAt.plusMinutes(40);
        //when
        Duration duration = orderSheet.calculateRemainingTtl(currentTime);
        //then
        assertThat(duration).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("상품 쿠폰이 적용된 주문 항목을 조회한다.")
    void findOrderSheetItemsWithAppliedItemCoupon(){
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        OrderSheetItem item = orderSheet.getItems().getFirst();

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", couponDiscountPolicy, 1);

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointPolicy);
        //when
        List<OrderSheetItem> result = orderSheet.findOrderSheetItemsWithAppliedItemCoupon();
        //then
        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting("id")
                .containsExactly(item.getId());
    }

    @Test
    @DisplayName("장바구니 쿠폰 적용 유무를 반환한다.")
    void hasCoupon() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCouponSnapshot = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", couponDiscountPolicy, Money.wons(10000L));

        orderSheet.applyCartCoupon(cartCouponSnapshot, pointPolicy);
        //when
        boolean hasCoupon = orderSheet.hasCoupon();
        //then
        assertThat(hasCoupon).isTrue();
    }
    
    @Test
    @DisplayName("장바구니 쿠폰 동일하면 예외가 발생하지 않는다.")
    void validateCartCouponNotChanged_whenCartCouponMatches_thenNotThrow() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCouponSnapshot = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", couponDiscountPolicy, Money.wons(10000L));

        orderSheet.applyCartCoupon(cartCouponSnapshot, pointPolicy);
        //when
        //then
        assertDoesNotThrow(() -> orderSheet.validateCartCouponNotChanged(cartCouponSnapshot));
    }

    @Test
    @DisplayName("장바구니 쿠폰이 적용되어있지 않은 주문서에 검증을 수행하면 예외가 발생한다.")
    void validateCartCouponNotChanged_whenNotAppliedCartCoupon_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCouponSnapshot = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", couponDiscountPolicy, Money.wons(10000L));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateCartCouponNotChanged(cartCouponSnapshot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 주문서에는 장바구니 쿠폰이 적용되어있지 않습니다");
    }

    @Test
    @DisplayName("주문서의 장바구니 쿠폰의 아이디가 동일하지 않으면 예외가 발생한다")
    void validateCartCouponNotChanged_whenMismatchCartCouponId_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy oldDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CouponDiscountPolicy newDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot oldCartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", oldDiscountPolicy, Money.wons(10000L));
        CartCouponSnapshot newCartCoupon = CartCouponSnapshot.of(2L, "장바구니 1000원 할인 쿠폰", newDiscountPolicy, Money.wons(10000L));

        orderSheet.applyCartCoupon(oldCartCoupon, pointPolicy);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateCartCouponNotChanged(newCartCoupon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검증하려는 쿠폰 ID가 주문서에 적용된 장바구니 쿠폰 ID와 일치하지 않습니다.");
    }

    @Test
    @DisplayName("주문서의 장바구니 쿠폰의 할인 정책이 동일하지 않으면 예외가 발생한다.")
    void validateCartCouponNotChanged_whenMismatchCartCouponPolicy_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy oldDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CouponDiscountPolicy newDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(2000L));
        CartCouponSnapshot oldCartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", oldDiscountPolicy, Money.wons(10000L));
        CartCouponSnapshot newCartCoupon = CartCouponSnapshot.of(1L, "장바구니 2000원 할인 쿠폰", newDiscountPolicy, Money.wons(10000L));

        orderSheet.applyCartCoupon(oldCartCoupon, pointPolicy);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateCartCouponNotChanged(newCartCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.COUPON_POLICY_CHANGED);
    }

    @Test
    @DisplayName("장바구니 쿠폰의 최소 결제금액이 동일하지 않으면 예외가 발생한다.")
    void validateCartCouponNotChanged_whenMismatchMinimumPaymentAmount_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        CouponDiscountPolicy oldDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CouponDiscountPolicy newDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot oldCartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", oldDiscountPolicy, Money.wons(10000L));
        CartCouponSnapshot newCartCoupon = CartCouponSnapshot.of(1L, "장바구니 2000원 할인 쿠폰", newDiscountPolicy, Money.wons(20000L));

        orderSheet.applyCartCoupon(oldCartCoupon, pointPolicy);
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validateCartCouponNotChanged(newCartCoupon))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.COUPON_POLICY_CHANGED);
    }

    @Test
    @DisplayName("포인트가 최대 적용 가능 한도를 만족하면 예외가 발생하지 않는다.")
    void validatePointsLimit_whenMetAvailablePoints_thenNotThrow() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertDoesNotThrow(() -> orderSheet.validatePointsLimit(Money.wons(1000L), pointPolicy));
    }

    @Test
    @DisplayName("포인트가 최대 적용 가능 한도를 초과하면 예외가 발생한다.")
    void validatePointsLimit_whenExceedAvailablePoints_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
        //when
        //then
        assertThatThrownBy(() -> orderSheet.validatePointsLimit(Money.wons(5000L), pointPolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
    }

    private CreateOrderSheetItemContext createOrderSheetItemContext(Long productVariantId) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, productVariantId, "SKU", "상품", "product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;

        return new CreateOrderSheetItemContext(
                productSnapshot, priceSnapshot, quantity, Collections.emptyList()
        );
    }
}
