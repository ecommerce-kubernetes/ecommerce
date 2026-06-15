package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 상품 도메인
 * 주문서 상품 정보를 담당하는 도메인
 * @author 최민식
 * @since 2026. 05. 24
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItem {
    private String sheetItemId;
    private ProductSnapshot productSnapshot;
    private ProductPriceSnapshot itemPrice;
    private OrderCouponSnapshot itemCoupon;
    private Integer quantity;
    private List<ProductOptionSnapshot> options;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(String sheetItemId, ProductSnapshot productSnapshot, ProductPriceSnapshot itemPrice, OrderCouponSnapshot itemCoupon, Integer quantity, List<ProductOptionSnapshot> options) {
        this.sheetItemId = sheetItemId;
        this.productSnapshot = productSnapshot;
        this.itemPrice = itemPrice;
        this.itemCoupon = itemCoupon;
        this.quantity = quantity;
        this.options = options;
    }

    /**
     * 주문 상품 정적 팩토리 메서드
     * <p>
     *     주문 상품 정보를 통해 주문서 상품 도메인을 생성하는 정적 팩토리 메서드
     * </p>
     * @param sheetItemId 주문 상품 아이디
     * @param productSnapshot 상품 정보
     * @param itemPrice 상품 가격 정보
     * @param coupon 상품 쿠폰 정보
     * @param quantity 주문 수량
     * @param options 상품 옵션
     * @return 주문 상품 도메인
     */
    public static OrderSheetItem create(String sheetItemId, ProductSnapshot productSnapshot, ProductPriceSnapshot itemPrice,
                                        OrderCouponSnapshot coupon, Integer quantity, List<ProductOptionSnapshot> options) {
        if ( quantity <= 0) {
            throw new BusinessException(OrderErrorCode.QUANTITY_MUST_BE_GREATER_THAN_ZERO);
        }
        return OrderSheetItem.reconstitute()
                .sheetItemId(sheetItemId)
                .productSnapshot(productSnapshot)
                .itemPrice(itemPrice)
                .itemCoupon(coupon)
                .quantity(quantity)
                .options(options)
                .build();
    }

    /**
     * 주문 상품의 총 가격
     * <p>
     * 주문 상품의 총 주문 가격을 반환한다
     * </p>
     *
     * @return 주문 상품 총 주문 가격
     */
    public Money getProductLineTotal() {
        return itemPrice.getDiscountedPrice().multiple(quantity);
    }

    /**
     * 주문 상품 쿠폰 할인 금액
     * <p>
     * 주문 상품 적용 쿠폰 할인 금액을 반환한다
     * </p>
     *
     * @return 주문 상품 쿠폰 할인 금액
     */
    public Money getAppliedCouponDiscount() {
        Money productTotal = getProductLineTotal();
        Money couponAmount = itemCoupon.getDiscountAmount();
        return productTotal.min(couponAmount);
    }

    /**
     * 총 주문 상품 할인 금액
     * <p>
     * 총 주문 상품 할인 금액을 반환한다
     * </p>
     *
     * @return 총 주문 상품 할인 금액
     */
    public Money getDiscountLineTotal() {
        return itemPrice.getDiscountAmount().multiple(quantity);
    }

    /**
     * 총 주문 상품 원본 금액
     * <p>
     * 총 주문 상품의 원본 금액을 반환한다
     * </p>
     *
     * @return 총 주문 상품 원본 금액
     */
    public Money getOriginalLineTotal() {
        return itemPrice.getOriginalPrice().multiple(quantity);
    }

    /**
     * 총 주문 상품의 쿠폰 적용 금액
     * <p>
     * 총 주문 상품의 쿠폰 적용 금액을 반환한다
     * </p>
     *
     * @return 총 주문 상품 쿠폰 적용 금액
     */
    public Money getFinalLineTotal() {
        return getProductLineTotal().subtract(getAppliedCouponDiscount());
    }

    /**
     * 주문 상품의 상품 변형 아이디
     * <p>
     * 주문 상품의 상품 변형 아이디를 반환한다
     * </p>
     *
     * @return 주문 상품 변형 아이디
     */
    public Long getProductVariantId() {
        return this.productSnapshot.getProductVariantId();
    }

    /**
     * 주문 상품의 상품 쿠폰 아이디
     * <p>
     * 주문 상품의 상품 쿠폰 아이디를 반환한다
     * </p>
     *
     * @return 주문 상품 쿠폰 아이디
     */
    public Long getCouponId() {
        return this.getItemCoupon().getCouponId();
    }

    /**
     * 주문 상품 판매 금액
     * <p>
     * 주문 상품의 판매 금액을 반환한다
     * </p>
     *
     * @return 주문 상품 판매 금액
     */
    public Money getDiscountedPrice() {
        return this.getItemPrice().getDiscountedPrice();
    }

    /**
     * 주문 상품 쿠폰 변경
     * <p>
     * 주문 상품 쿠폰을 변경한다
     * </p>
     *
     * @param itemCoupon 주문 상품 쿠폰 정보
     */
    public void changeCoupon(OrderCouponSnapshot itemCoupon) {
        this.itemCoupon = itemCoupon;
    }

    public boolean hasCoupon() {
        return this.itemCoupon.getCouponId() != null;
    }
}
