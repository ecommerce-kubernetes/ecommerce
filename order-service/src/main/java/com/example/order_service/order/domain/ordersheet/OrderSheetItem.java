package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItem {

    private Long id;

    private ProductSnapshot productSnapshot;

    private ProductPriceSnapshot priceSnapshot;

    private ItemCouponSnapshot itemCouponSnapshot;

    private int quantity;

    private List<ProductOptionSnapshot> optionSnapshots;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(Long id, ProductSnapshot productSnapshot, ProductPriceSnapshot priceSnapshot, ItemCouponSnapshot itemCoupon,
                           int quantity, List<ProductOptionSnapshot> optionSnapshots) {
        Assert.notNull(id, "주문 항목(OrderSheetItem) 생성시 아이디는 필수이다.");
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

    public static OrderSheetItem create(CreateOrderSheetItemContext context, IdGenerator idGenerator) {
        if (context.quantity() <= 0) {
            throw new BusinessException(OrderErrorCode.INVALID_ITEM_QUANTITY);
        }

        Long id = idGenerator.generate();

        return OrderSheetItem.reconstitute()
                .id(id)
                .productSnapshot(context.productSnapshot())
                .priceSnapshot(context.priceSnapshot())
                .quantity(context.quantity())
                .optionSnapshots(context.optionSnapshots())
                .build();
    }

    void applyItemCoupon(ItemCouponSnapshot itemCoupon) {
        Assert.notNull(itemCoupon, "적용할 쿠폰 정보는 필수 입니다.");
        this.itemCouponSnapshot = itemCoupon;
    }

    void removeItemCoupon() {
        this.itemCouponSnapshot = null;
    }

    public Money calculateOriginalLineTotal() {
        return priceSnapshot.getOriginalPrice().multiple(quantity);
    }

    public Money calculateItemDiscountLineTotal() {
        return priceSnapshot.getDiscountAmount().multiple(quantity);
    }

    public Money calculateLineTotal() {
        return priceSnapshot.getDiscountedPrice().multiple(quantity);
    }

    public Money calculateCouponDiscount() {
        if (!hasCoupon()) {
            return Money.ZERO;
        }

        Money couponDiscount = this.itemCouponSnapshot.calculateTotalDiscount(priceSnapshot.getDiscountedPrice(), quantity);
        Money lineTotal = calculateLineTotal();
        return Money.min(couponDiscount, lineTotal);
    }

    public Money calculateFinalAmount() {
        return calculateLineTotal().subtract(calculateCouponDiscount());
    }

    public Long getProductVariantId() {
        return this.productSnapshot.getProductVariantId();
    }

    public boolean hasCoupon() {
        return this.itemCouponSnapshot != null;
    }

    public void validatePriceNotChanged(ProductPriceSnapshot currentPriceSnapshot) {
        if (!this.priceSnapshot.equals(currentPriceSnapshot)) {
            throw new BusinessException(OrderErrorCode.PRODUCT_PRICE_CHANGED);
        }
    }

    public void validateItemCouponNotChanged(ItemCouponSnapshot currentItemCouponSnapshot) {
        if (!this.hasCoupon()) {
            throw new IllegalStateException("해당 주문 항목에는 쿠폰이 적용되어있지 않습니다.");
        }

        if (!this.itemCouponSnapshot.getItemCouponId().equals(currentItemCouponSnapshot.getItemCouponId())) {
            throw new IllegalArgumentException("검증하려는 쿠폰 ID가 주문 항목에 적용된 상품 쿠폰 ID와 일치하지 않습니다.");
        }

        if (!this.itemCouponSnapshot.getDiscountPolicy().equals(currentItemCouponSnapshot.getDiscountPolicy()) ||
                !this.itemCouponSnapshot.getApplyQuantityLimit().equals(currentItemCouponSnapshot.getApplyQuantityLimit())) {
            throw new BusinessException(OrderErrorCode.COUPON_POLICY_CHANGED);
        }
    }
}
