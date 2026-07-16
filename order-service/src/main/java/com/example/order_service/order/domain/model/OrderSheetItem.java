package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItem {
    private String id;
    private ProductSnapshot productSnapshot;
    private ProductPriceSnapshot priceSnapshot;
    private ItemCouponSnapshot itemCouponSnapshot;
    private int quantity;
    private List<ProductOptionSnapshot> optionSnapshots;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(String id, ProductSnapshot productSnapshot, ProductPriceSnapshot priceSnapshot, ItemCouponSnapshot itemCoupon,
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

    public Money getOriginalLineTotal() {
        return priceSnapshot.getOriginalPrice().multiple(quantity);
    }

    public Money getProductDiscountLineTotal() {
        return priceSnapshot.getDiscountAmount().multiple(quantity);
    }

    public Money getLineTotal() {
        return priceSnapshot.getDiscountedPrice().multiple(quantity);
    }

    public Money getCouponDiscount() {
        if (this.itemCouponSnapshot == null) {
            return Money.ZERO;
        }
        Money couponDiscount = this.itemCouponSnapshot.calculateTotalDiscount(priceSnapshot.getDiscountedPrice(), quantity);
        Money lineTotal = getLineTotal();
        return Money.min(couponDiscount, lineTotal);
    }

    public Money getFinalAmount() {
        return getLineTotal().subtract(getCouponDiscount());
    }

    protected void applyItemCoupon(ItemCouponSnapshot itemCoupon) {
        this.itemCouponSnapshot = itemCoupon;
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
        return this.getItemCouponSnapshot().getItemCouponId();
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
    protected void changeItemCoupon(ItemCouponSnapshot newItemCoupon) {
        this.itemCouponSnapshot = newItemCoupon;
    }

    public boolean hasCoupon() {
        return this.itemCouponSnapshot.getItemCouponId() != null;
    }
}
