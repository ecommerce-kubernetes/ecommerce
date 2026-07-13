package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

/**
 * 주문 상품 도메인
 * 주문서 상품 정보를 담당하는 도메인
 * @author 최민식
 * @since 2026. 05. 24
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItem {
    private String id;
    private ProductSnapshot productSnapshot;
    private ProductPriceSnapshot priceSnapshot;
    private OrderCouponSnapshot itemCouponSnapshot;
    private int quantity;
    private List<ProductOptionSnapshot> optionSnapshots;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(String id, ProductSnapshot productSnapshot, ProductPriceSnapshot priceSnapshot, OrderCouponSnapshot itemCoupon,
                           int quantity, List<ProductOptionSnapshot> optionSnapshots) {
        Assert.hasText(id, "주문 항목(OrderSheetItem) 생성시 아이디는 필수이다.");
        Assert.notNull(productSnapshot, "주문 항목(OrderSheetItem) 생성시 상품 정보는 필수이다.");
        Assert.notNull(priceSnapshot, "주문 항목(OrderSheetItem) 생성시 상품 가격은 필수이다.");
        Assert.notNull(optionSnapshots, "주문 항목(OrderSheetItem) 생성시 상품 옵션은 필수이다.");

        this.id = id;
        this.productSnapshot = productSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.itemCouponSnapshot = itemCoupon;
        this.quantity = quantity;
        this.optionSnapshots = optionSnapshots;
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
                .id(sheetItemId)
                .productSnapshot(productSnapshot)
                .priceSnapshot(itemPrice)
                .itemCoupon(coupon)
                .quantity(quantity)
                .optionSnapshots(options)
                .build();
    }

    public static OrderSheetItem create(ProductSnapshot productSnapshot, ProductPriceSnapshot priceSnapshot, int quantity, List<ProductOptionSnapshot> optionSnapshots) {
        String id = UUID.randomUUID().toString();

        if (quantity <= 0) {
            throw new BusinessException(OrderErrorCode.INVALID_ITEM_QUANTITY);
        }

        return OrderSheetItem.reconstitute()
                .id(id)
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(quantity)
                .optionSnapshots(optionSnapshots)
                .build();
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
        Money productTotal = getLineTotal();
        Money couponAmount = itemCouponSnapshot.getDiscountAmount();
        return productTotal.min(couponAmount);
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
        return priceSnapshot.getOriginalPrice().multiple(quantity);
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
        return priceSnapshot.getDiscountAmount().multiple(quantity);
    }

    /**
     * 주문 상품의 총 가격
     * <p>
     * 주문 상품의 총 주문 가격을 반환한다
     * </p>
     *
     * @return 주문 상품 총 주문 가격
     */
    public Money getLineTotal() {
        return priceSnapshot.getDiscountedPrice().multiple(quantity);
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
        return getLineTotal().subtract(getAppliedCouponDiscount());
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
        return this.getItemCouponSnapshot().getCouponId();
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
        return this.getPriceSnapshot().getDiscountedPrice();
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
        this.itemCouponSnapshot = itemCoupon;
    }

    public boolean hasCoupon() {
        return this.itemCouponSnapshot.getCouponId() != null;
    }
}
