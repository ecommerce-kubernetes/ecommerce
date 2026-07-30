package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheet {

    private Long id;

    private Orderer orderer;

    private ShippingAddress shippingAddress;

    private List<OrderSheetItem> items;

    private CartCouponSnapshot cartCoupon;

    private Money usedPoints;

    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(Long id, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, CartCouponSnapshot cartCoupon,
                       Money usedPoints, LocalDateTime expiresAt) {
        Assert.notNull(id, "주문서(OrderSheet) 생성시 아이디는 필수이다.");
        Assert.notNull(orderer, "주문서(OrderSheet) 생성시 주문자는 필수이다.");
        Assert.notNull(items, "주문서(OrderSheet) 생성시 주문 항목은 필수이다.");
        Assert.notNull(usedPoints, "주문서(OrderSheet) 생성시 적용 포인트 금액은 필수이다.");
        Assert.notNull(expiresAt, "주문서(OrderSheet) 생성시 만료 시간은 필수이다.");

        this.id = id;
        this.orderer = orderer;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.cartCoupon = cartCoupon;
        this.usedPoints = usedPoints;
        this.expiresAt = expiresAt;
    }

    public static OrderSheet create(CreateOrderSheetContext context, IdGenerator idGenerator) {
        if (context.items().isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }

        Long id = idGenerator.generate();

        List<OrderSheetItem> orderSheetItems = context.items().stream().map(itemCtx -> OrderSheetItem.create(itemCtx, idGenerator))
                .toList();

        return OrderSheet.reconstitute()
                .id(id)
                .orderer(context.orderer())
                .items(orderSheetItems)
                .usedPoints(Money.ZERO)
                .expiresAt(context.expiresAt())
                .build();
    }

    public static OrderSheet create(Orderer orderer, List<OrderSheetItem> items, LocalDateTime expiresAt, IdGenerator idGenerator) {
        if (items.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }

        Long id = idGenerator.generate();

        return OrderSheet.reconstitute()
                .id(id)
                .orderer(orderer)
                .items(items)
                .usedPoints(Money.ZERO)
                .expiresAt(expiresAt)
                .build();
    }

    public void changeShippingAddress(ShippingAddress newAddress) {
        Assert.notNull(newAddress, "변경할 배송 정보는 필수 입니다.");
        this.shippingAddress = newAddress;
    }

    public void applyItemCoupon(Long orderSheetItemId, ItemCouponSnapshot itemCoupon, PointUsagePolicy pointPolicy) {
        OrderSheetItem item = findOrderSheetItem(orderSheetItemId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_SHEET_ITEM_NOT_FOUND));
        item.applyItemCoupon(itemCoupon);

        adjustUsedPointsByPolicy(pointPolicy);
    }

    public void applyCartCoupon(CartCouponSnapshot cartCoupon, PointUsagePolicy pointPolicy) {
        Assert.notNull(cartCoupon, "적용할 쿠폰 정보는 필수 입니다.");
        Money subTotal = calculateSubTotal();
        if (!cartCoupon.isSatisfiedBy(subTotal)) {
            throw new BusinessException(OrderErrorCode.CART_COUPON_MINIMUM_PAYMENT_NOT_MET);
        }
        this.cartCoupon = cartCoupon;

        adjustUsedPointsByPolicy(pointPolicy);
    }

    public void applyPoints(Money usedPoints, PointUsagePolicy policy) {
        Money maxUsablePoints = calculateMaxUsablePoints(policy);

        if (usedPoints.isGreaterThan(maxUsablePoints)) {
            throw new BusinessException(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
        }

        this.usedPoints = usedPoints;
    }

    private Optional<OrderSheetItem> findOrderSheetItem(Long orderSheetItemId) {
        return this.items.stream().filter(item -> item.getId().equals(orderSheetItemId))
                .findFirst();
    }

    private Money calculateSubTotal() {
        return this.items.stream()
                .map(OrderSheetItem::calculateLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private Money calculateTotalFinalAmount() {
        return this.items.stream()
                .map(OrderSheetItem::calculateFinalAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money calculateCartCouponDiscount() {
        if (this.cartCoupon == null) {
            return Money.ZERO;
        }

        Money totalFinalAmount = calculateTotalFinalAmount();
        Money discount = cartCoupon.calculateDiscount(totalFinalAmount);

        return Money.min(totalFinalAmount, discount);
    }

    private void adjustUsedPointsByPolicy(PointUsagePolicy pointPolicy) {
        Money maxUsablePoints = calculateMaxUsablePoints(pointPolicy);

        if (this.usedPoints.isGreaterThan(maxUsablePoints)) {
            this.usedPoints = maxUsablePoints;
        }
    }

    public boolean isExpired(LocalDateTime currentTime) {
        return currentTime.isAfter(this.expiresAt);
    }

    public Money calculateMaxUsablePoints(PointUsagePolicy pointPolicy) {
        Money totalFinalAmount = calculateTotalFinalAmount();
        Money cartCouponDiscount = calculateCartCouponDiscount();
        Money pointApplicableAmount = totalFinalAmount.subtract(cartCouponDiscount);
        return pointPolicy.calculateAvailablePoints(pointApplicableAmount);
    }

    public Money calculateTotalOriginalAmount() {
        return this.items.stream()
                .map(OrderSheetItem::calculateOriginalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    public Money calculateTotalItemDiscount() {
        return this.items.stream()
                .map(OrderSheetItem::calculateItemDiscountLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    public Money calculateTotalItemCouponDiscount() {
        return this.items.stream()
                .map(OrderSheetItem::calculateCouponDiscount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money calculateTotalPaymentAmount() {
        Money totalFinalAmount = calculateTotalFinalAmount();
        Money cartCouponDiscount = calculateCartCouponDiscount();
        Money pointApplicableAmount = totalFinalAmount.subtract(cartCouponDiscount);
        return pointApplicableAmount.subtract(usedPoints);
    }

    public Duration calculateRemainingTtl(LocalDateTime currentTime) {
        Duration remaining = Duration.between(currentTime, this.expiresAt);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * 주문서에 쿠폰이 적용되어있는지 확인
     * <p>
     * 주문서에 장바구니 쿠폰 또는 상품 쿠폰이 적용되어있는지 확인
     * </p>
     *
     * @return 쿠폰 적용 여부
     */
    public boolean hasAnyCoupon() {
        return hasCartCoupon() || hasItemCoupon();
    }

    /**
     * 주문서에 장바구니 쿠폰이 적용되어있는지 확인
     * <p>
     * 주문서에 장바구니 쿠폰이 적용되어있는지 확인한다
     * </p>
     *
     * @return 장바구니 쿠폰 적용 여부
     */
    public boolean hasCartCoupon() {
        return this.cartCoupon.getCartCouponId() != null;
    }

    /**
     * 주문 상품에 상품 쿠폰이 적용된 상품이 있는지 확인
     * <p>
     * 주문 상품중 상품 쿠폰이 적용된 상품이 있는지 확인한다
     * </p>
     *
     * @return 상품 쿠폰 적용 여부
     */
    public boolean hasItemCoupon() {
        return this.items.stream()
                .anyMatch(OrderSheetItem::hasCoupon);
    }
}
