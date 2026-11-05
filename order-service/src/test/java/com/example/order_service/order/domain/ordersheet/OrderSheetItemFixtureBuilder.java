package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OrderSheetItemFixtureBuilder {

    private ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
    private ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
    private ItemCouponSnapshot itemCouponSnapshot;
    private int quantity = 1;
    private List<ProductOptionSnapshot> options = List.of(ProductOptionSnapshot.of("사이즈", "XL"));

    private static AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    public static OrderSheetItemFixtureBuilder given() {
        return new OrderSheetItemFixtureBuilder();
    }

    public OrderSheetItemFixtureBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderSheetItemFixtureBuilder withPriceSnapshot(ProductPriceSnapshot priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
        return this;
    }

    public OrderSheetItemFixtureBuilder withFixedItemCoupon(Long id, Money discount, int applyMaxQuantity) {
        this.itemCouponSnapshot = ItemCouponSnapshot.of(id, "상품 쿠폰", new FixedCouponDiscountPolicy(discount), applyMaxQuantity);
        return this;
    }

    public OrderSheetItem build() {
        CreateOrderSheetItemContext context = CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(quantity)
                .optionSnapshots(options)
                .build();

        OrderSheetItem orderSheetItem = OrderSheetItem.create(context, idGenerator);

        if (this.itemCouponSnapshot != null) {
            orderSheetItem.applyItemCoupon(itemCouponSnapshot);
        }

        return orderSheetItem;
    }
}
