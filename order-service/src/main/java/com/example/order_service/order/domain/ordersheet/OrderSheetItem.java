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

    public static OrderSheetItem create(ProductSnapshot productSnapshot, ProductPriceSnapshot priceSnapshot,
                                        int quantity, List<ProductOptionSnapshot> optionSnapshots, IdGenerator idGenerator) {
        if (quantity <= 0) {
            throw new BusinessException(OrderErrorCode.INVALID_ITEM_QUANTITY);
        }

        Long id = idGenerator.generate();

        return OrderSheetItem.reconstitute()
                .id(id)
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(quantity)
                .optionSnapshots(optionSnapshots)
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
        if (this.itemCouponSnapshot == null) {
            return Money.ZERO;
        }
        Money couponDiscount = this.itemCouponSnapshot.calculateTotalDiscount(priceSnapshot.getDiscountedPrice(), quantity);
        Money lineTotal = calculateLineTotal();
        return Money.min(couponDiscount, lineTotal);
    }

    public Money calculateFinalAmount() {
        return calculateLineTotal().subtract(calculateCouponDiscount());
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

    public boolean hasCoupon() {
        return this.itemCouponSnapshot.getItemCouponId() != null;
    }

}
