package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.example.order_service.ordersheet.domain.model.vo.OrderCouponSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.Orderer;
import com.example.order_service.ordersheet.domain.model.vo.ShippingAddress;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    private String sheetId;
    private Orderer orderer;
    private ShippingAddress shippingAddress;
    private List<OrderSheetItem> items;
    private OrderCouponSnapshot cartCoupon;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalCouponDiscountAmount;
    private Money usedPoints;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String sheetId, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, OrderCouponSnapshot cartCoupon,
                       Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                       Money usedPoints, Money totalPaymentAmount, LocalDateTime expiresAt) {
        this.sheetId = sheetId;
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

    /**
     * 주문서 정적 팩토리 메서드
     * <p>
     * 주문 정보를 통해 주문서 도메인을 생성하는 정적 팩토리 메서드
     * </p>
     *
     * @param sheetId         주문서 아이디
     * @param orderer         주문자 정보(VO)
     * @param shippingAddress 배송 정보(VO)
     * @param items           주문 아이템
     * @param coupon          장바구니 쿠폰
     * @param createdAt       생성시간
     * @param ttl             주문서 만료 기간
     * @return 주문서 애그리거트 루트 도메인
     */
    public static OrderSheet create(String sheetId, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, OrderCouponSnapshot coupon, LocalDateTime createdAt, long ttl) {
        if (items == null || items.isEmpty()) {
            throw new InvalidDomainValueException("OrderSheet 주문 상품은 필수입니다");
        }

        Money pointEligibleAmount = calcPointEligibleAmount(items, coupon);

        return OrderSheet.reconstitute()
                .sheetId(sheetId)
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(items)
                .cartCoupon(coupon)
                .totalOriginalPrice(calcTotalOriginalPrice(items))
                .totalProductDiscountAmount(calcTotalProductDiscountAmount(items))
                .totalCouponDiscountAmount(calcAppliedCartCouponDiscount(items, coupon).add(calcTotalItemCouponDiscountAmount(items)))
                .usedPoints(Money.ZERO)
                .totalPaymentAmount(pointEligibleAmount)
                .expiresAt(createdAt.plusMinutes(ttl))
                .build();
    }

    public Money getPointEligibleAmount() {
        return calcPointEligibleAmount(this.items, this.cartCoupon);
    }

    private static Money calcTotalItemFinalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getFinalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcPointEligibleAmount(List<OrderSheetItem> items, OrderCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        return itemFinalPrice.isLessThan(coupon.getDiscountAmount()) ?
                Money.ZERO : itemFinalPrice.subtract(coupon.getDiscountAmount());
    }

    private static Money calcAppliedCartCouponDiscount(List<OrderSheetItem> items, OrderCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        return itemFinalPrice.isLessThan(coupon.getDiscountAmount()) ? itemFinalPrice : coupon.getDiscountAmount();
    }

    private static Money calcTotalOriginalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getOriginalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcTotalProductDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getDiscountLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcTotalItemCouponDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getAppliedCouponDiscount)
                .reduce(Money.ZERO, Money::add);
    }

    /**
     * 주문자 확인
     * <p>
     * 유저 아이디와 주문자의 유저 아이디의 일치 여부를 반환
     * </p>
     *
     * @param userId 유저 아이디
     * @return 일치 여부
     */
    public boolean isOwner(Long userId) {
        return this.orderer.getUserId().equals(userId);
    }

    /**
     * 주문서 만료 확인
     * <p>
     * 현재 주문서의 만료 여부를 반환
     * </p>
     *
     * @return 주문서 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 주문서의 남은 만료 시간 반환
     * <p>
     * 현재 주문서의 만료 까지 남은 시간을 반환
     * </p>
     *
     * @return 만료까지 남은 시간
     */
    public Duration getRemainingTtl() {
        return Duration.between(LocalDateTime.now(), this.expiresAt);
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
     * @param usedPoints 적용 포인트
     * @throws InvalidDomainValueException 도메인 계층 예외
     */
    public void changeUsedPoints(Money usedPoints) {
        Money eligibleAmount = getPointEligibleAmount();
        if (usedPoints.isGreaterThan(eligibleAmount)) {
            throw new InvalidDomainValueException("적용 포인트가 주문 결제 대상 금액을 초과할 수 없습니다");
        }
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = eligibleAmount.subtract(usedPoints);
    }

    /**
     * 주문서의 주문 상품을 반환
     * <p>
     * 주문서의 주문 상품중 파라미터의 상품 아이디와 동일한 주문 상품을 반환
     * </p>
     *
     * @param sheetItemId 주문 상품 아이디
     * @return 주문 상품
     * @throws InvalidDomainValueException 도메인 계층 예외
     */
    public OrderSheetItem getItem(String sheetItemId) {
        return this.items.stream()
                .filter(item -> item.getSheetItemId().equals(sheetItemId))
                .findFirst()
                .orElseThrow(() -> new InvalidDomainValueException("주문 상품을 찾을 수 없음"));
    }

    public Money calcEstimatedPointEligibleAmount(String sheetItemId, OrderCouponSnapshot itemCouponSnapshot) {
        Money estimatedItemFinalPrice = this.items.stream().map(item -> {
            if (item.getSheetItemId().equals(sheetItemId)) {
                Money productLineTotal = item.getProductLineTotal();
                Money discount = productLineTotal.subtract(itemCouponSnapshot.getDiscountAmount());
                return productLineTotal.subtract(discount);
            }
            return item.getFinalLineTotal();
        }).reduce(Money.ZERO, Money::add);
        return estimatedItemFinalPrice.subtract(cartCoupon.getDiscountAmount());
    }

    public Money calcEstimatedPointEligibleAmount(OrderCouponSnapshot cartCoupon) {
        Money itemFinalPrice = this.items.stream().map(OrderSheetItem::getFinalLineTotal).reduce(Money.ZERO, Money::add);
        return itemFinalPrice.subtract(cartCoupon.getDiscountAmount());
    }

    public void changeItemCoupon(String sheetItemId, OrderCouponSnapshot newCouponSnapshot, Money maxAvailablePoints) {
        OrderSheetItem sheetItem = getItem(sheetItemId);
        sheetItem.changeCoupon(newCouponSnapshot);
        this.totalCouponDiscountAmount = calcAppliedCartCouponDiscount(items, cartCoupon).add(calcTotalItemCouponDiscountAmount(items));
        if (this.usedPoints.equals(Money.ZERO)) return;
        Money pointEligibleAmount = getPointEligibleAmount();
        Money trueMaxLimit = pointEligibleAmount.isLessThan(maxAvailablePoints) ? pointEligibleAmount : maxAvailablePoints;
        if (this.usedPoints.isGreaterThan(trueMaxLimit)) {
            this.usedPoints = trueMaxLimit;
        }
        this.totalPaymentAmount = pointEligibleAmount.subtract(usedPoints);
    }

    public void changeCartCoupon(OrderCouponSnapshot newCartCouponSnapshot, Money maxAvailablePoints) {
        this.cartCoupon = newCartCouponSnapshot;
        this.totalCouponDiscountAmount = calcAppliedCartCouponDiscount(items, cartCoupon).add(calcTotalItemCouponDiscountAmount(items));
        if (this.usedPoints.equals(Money.ZERO)) return;
        Money pointEligibleAmount = getPointEligibleAmount();
        Money trueMaxLimit = pointEligibleAmount.isLessThan(maxAvailablePoints) ? pointEligibleAmount : maxAvailablePoints;
        if (this.usedPoints.isGreaterThan(trueMaxLimit)) {
            this.usedPoints = trueMaxLimit;
        }
        this.totalPaymentAmount = pointEligibleAmount.subtract(usedPoints);
    }
}
