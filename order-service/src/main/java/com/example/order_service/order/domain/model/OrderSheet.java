package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
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
import java.util.UUID;

/**
 * 주문서 도메인
 * <p>
 * 주문서 도메인
 * 주문자, 주문 배송, 장바구니 쿠폰, 주문 상품 정보를 담당하는 애그리거트 루트 도메인
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheet {
    private String id;
    private Orderer orderer;
    private ShippingAddress shippingAddress;
    private List<OrderSheetItem> items;
    private CartCouponSnapshot cartCoupon;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalCouponDiscountAmount;
    private Money usedPoints;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String id, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, CartCouponSnapshot cartCoupon,
                       Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                       Money usedPoints, Money totalPaymentAmount, LocalDateTime expiresAt) {
        Assert.hasText(id, "주문서(OrderSheet) 생성시 아이디는 필수이다.");
        Assert.notNull(orderer, "주문서(OrderSheet) 생성시 주문자는 필수이다.");
        Assert.notNull(items, "주문서(OrderSheet) 생성시 주문 항목은 필수이다.");
        Assert.notNull(expiresAt, "주문서(OrderSheet) 생성시 만료 시간은 필수이다.");

        this.id = id;
        this.orderer = orderer;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.cartCoupon = cartCoupon;
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscountAmount = totalProductDiscountAmount;
        this.totalCouponDiscountAmount = totalCouponDiscountAmount;
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = totalPaymentAmount;
        this.expiresAt = expiresAt;
    }

    public static OrderSheet create(Orderer orderer, List<OrderSheetItem> items, LocalDateTime expiresAt) {
        if (items.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }
        String id = UUID.randomUUID().toString();

        Money totalOriginalPrice = calculateTotalOriginalPrice(items);
        Money totalProductDiscountAmount = calculateTotalProductDiscountAmount(items);
        Money totalPaymentAmount = calculateTotalPaymentAmount(items);
        return OrderSheet.reconstitute()
                .id(id)
                .orderer(orderer)
                .items(items)
                .totalOriginalPrice(totalOriginalPrice)
                .totalProductDiscountAmount(totalProductDiscountAmount)
                .totalPaymentAmount(totalPaymentAmount)
                .expiresAt(expiresAt)
                .build();
    }

    private static Money calculateTotalOriginalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getOriginalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calculateTotalProductDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getProductDiscountLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calculateTotalPaymentAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcTotalItemCouponDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getAppliedCouponDiscount)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcAppliedCartCouponDiscount(List<OrderSheetItem> items, CartCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        // [NOTE]
        // 총 상품 쿠폰 적용 금액(상품 할인금액 - 상품 쿠폰할인금)이 장바구니 쿠폰 할인금보다 작은 경우
        // 장바구니 쿠폰 할인금액은 총 상품 쿠폰 적용 금액이 된다
        return itemFinalPrice.min(coupon.getDiscountAmount());
    }

    private static Money calcPointEligibleAmount(List<OrderSheetItem> items, CartCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        Money appliedCartDiscount = calcAppliedCartCouponDiscount(items, coupon);
        return itemFinalPrice.subtract(appliedCartDiscount);
    }

    private static Money calcTotalItemFinalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getFinalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * 주문 접근 확인
     * <p>
     * 주문서의 주문자가 파라미터의 유저 아이디와 같은지 검증,
     * 주문이 만료되었는지 검증
     * </p>
     *
     * @param userId 유저 아이디
     */
    public void validateAccess(Long userId, LocalDateTime currentTime) {
        if (!this.orderer.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }
        if (this.isExpired(currentTime)) {
            throw new BusinessException(OrderErrorCode.ORDER_EXPIRED);
        }
    }

    /**
     * 주문서 만료 확인
     * <p>
     * 현재 주문서의 만료 여부를 반환
     * </p>
     *
     * @return 주문서 만료 여부
     */
    public boolean isExpired(LocalDateTime currentTime) {
        return currentTime.isAfter(this.expiresAt);
    }

    /**
     * 주문서의 남은 만료 시간 반환
     * <p>
     * 현재 주문서의 만료 까지 남은 시간을 반환
     * </p>
     *
     * @return 만료까지 남은 시간
     */
    public Duration getRemainingTtl(LocalDateTime currentTime) {
        return Duration.between(currentTime, this.expiresAt);
    }

    /**
     * 주문서 배송 정보를 수정
     * <p>
     * 주문서 배송 정보를 파라미터 VO로 수정
     * </p>
     *
     * @param newAddress 배송 정보 VO
     */
    public void changeShippingAddress(ShippingAddress newAddress) {
        this.shippingAddress = newAddress;
    }

    /**
     * 적용 포인트 수정
     * <p>
     * 주문서의 적용 포인트를 파라미터 포인트로 적용한다
     * </p>
     *
     * @param usedPoints  적용 포인트
     * @param ownedPoints 보유중인 포인트
     * @param policy      포인트 할인 정책
     * @throws BusinessException 비지니스 예외
     */
    public void changeUsedPoints(Money usedPoints, Money ownedPoints, PointUsagePolicy policy) {
        Money availablePoints = calcAvailablePoints(ownedPoints, policy);
        if (usedPoints.isGreaterThan(availablePoints)) {
            throw new BusinessException(OrderErrorCode.ORDER_POINT_POLICY_VIOLATION);
        }
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = calcPointEligibleAmount(this.items, this.cartCoupon).subtract(usedPoints);
    }

    /**
     * 주문서의 주문 상품을 반환
     * <p>
     * 주문서의 주문 상품중 파라미터의 상품 아이디와 동일한 주문 상품을 반환
     * </p>
     *
     * @param sheetItemId 주문 상품 아이디
     * @return 주문 상품
     * @throws BusinessException 비지니스 예외
     */
    public OrderSheetItem getItem(String sheetItemId) {
        return this.items.stream()
                .filter(item -> item.getId().equals(sheetItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    /**
     * 사용 가능한 포인트를 반환
     * <p>
     * 주문에 사용할 수 있는 최대 포인트를 반환한다
     * </p>
     *
     * @param ownedPoints 보유 포인트
     * @param pointPolicy 포인트 정책
     * @return 사용할 수 있는 최대 포인트
     */
    public Money calcAvailablePoints(Money ownedPoints, PointUsagePolicy pointPolicy) {
        Money pointEligibleAmount = calcPointEligibleAmount(this.items, this.cartCoupon);
        Money pointsLimit = pointPolicy.calculateMaxLimit(pointEligibleAmount);
        return ownedPoints.min(pointsLimit);
    }

    /**
     * 상품 쿠폰을 변경한다
     * <p>
     * 상품에 적용된 상품 쿠폰을 새로운 상품 쿠폰으로 변경한다
     * 상품 쿠폰을 변경하여 주문서에 적용된 포인트가 적용 가능 최대 포인트를 초과하는 경우 적용 가능 최대 포인트로 조정된다
     * </p>
     *
     * @param sheetItemId       주문 상품 아이디
     * @param newCouponSnapshot 새 쿠폰 정보
     * @param ownedPoints       보유 포인트
     * @param pointPolicy       포인트 정책
     */
    public void changeItemCoupon(String sheetItemId, ItemCouponSnapshot newCouponSnapshot, Money ownedPoints, PointUsagePolicy pointPolicy) {
        OrderSheetItem sheetItem = getItem(sheetItemId);
        sheetItem.changeCoupon(newCouponSnapshot);
        recalculateTotals(ownedPoints, pointPolicy);
    }

    /**
     * 장바구니 쿠폰을 변경한다
     * <p>
     * 주문서에 적용된 장바구니 쿠폰을 새로운 장바구니 쿠폰으로 변경한다
     * 장바구니 쿠폰을 변경하여 주문서에 적용된 포인트가 적용 가능 최대 포인트를 초과하는 경우 적용 가능 최대 포인트로 조정된다
     * </p>
     *
     * @param newCartCouponSnapshot 새 장바구니 쿠폰 정보
     * @param ownedPoints           보유 포인트
     * @param pointPolicy           포인트 정책
     */
    public void changeCartCoupon(CartCouponSnapshot newCartCouponSnapshot, Money ownedPoints, PointUsagePolicy pointPolicy) {
        this.cartCoupon = newCartCouponSnapshot;
        recalculateTotals(ownedPoints, pointPolicy);
    }

    private void recalculateTotals(Money ownedPoints, PointUsagePolicy pointPolicy) {
        this.totalCouponDiscountAmount = calcAppliedCartCouponDiscount(this.items, this.cartCoupon)
                .add(calcTotalItemCouponDiscountAmount(this.items));
        Money pointEligibleAmount = calcPointEligibleAmount(this.items, this.cartCoupon);
        if (!this.usedPoints.equals(Money.ZERO)) {
            Money availablePoints = calcAvailablePoints(ownedPoints, pointPolicy);
            if (this.usedPoints.isGreaterThan(availablePoints)) {
                this.usedPoints = availablePoints;
            }
        }
        this.totalPaymentAmount = pointEligibleAmount.subtract(this.usedPoints);
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
        return this.cartCoupon.getCouponId() != null;
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
