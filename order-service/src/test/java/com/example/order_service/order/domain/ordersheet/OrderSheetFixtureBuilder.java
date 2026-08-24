package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OrderSheetFixtureBuilder {

    private Orderer orderer = Orderer.of(1L, "주문자","010-1234-5678");
    private ShippingAddress shippingAddress;
    private Money usedPoint;
    private PointUsagePolicy pointPolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
    private ItemCouponSnapshot itemCouponSnapshot;
    private CartCouponSnapshot cartCouponSnapshot;
    private LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(30);

    private List<CreateOrderSheetItemContext> itemContexts;

    private static AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    public static OrderSheetFixtureBuilder given() {
        OrderSheetFixtureBuilder builder = new OrderSheetFixtureBuilder();
        builder.itemContexts = builder.createDefaultOrderSheetItems();
        return builder;
    }

    public OrderSheetFixtureBuilder withItemContexts(CreateOrderSheetItemContext... contexts) {
        this.itemContexts = Arrays.asList(contexts);
        return this;
    }

    public OrderSheetFixtureBuilder setPointUsagePolicy(PointUsagePolicy pointUsagePolicy) {
        this.pointPolicy = pointUsagePolicy;
        return this;
    }

    public OrderSheetFixtureBuilder withUsedPoint(Money usedPoint) {
        this.usedPoint = usedPoint;
        return this;
    }

    public OrderSheetFixtureBuilder withExpiresAt(LocalDateTime expiresAt) {
        this.expiredAt = expiresAt;
        return this;
    }

    public OrderSheetFixtureBuilder withShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
        return this;
    }

    public OrderSheetFixtureBuilder withItemCoupon(ItemCouponSnapshot itemCoupon) {
        this.itemCouponSnapshot = itemCoupon;
        return this;
    }

    public OrderSheetFixtureBuilder withCartCoupon(CartCouponSnapshot cartCoupon) {
        this.cartCouponSnapshot = cartCoupon;
        return this;
    }

    public OrderSheet build() {
        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(this.orderer)
                .shippingAddress(this.shippingAddress)
                .items(this.itemContexts)
                .expiresAt(this.expiredAt)
                .build();

        OrderSheet orderSheet = OrderSheet.create(context, idGenerator);

        if (itemCouponSnapshot != null && !orderSheet.getItems().isEmpty()) {
            orderSheet.applyItemCoupon(orderSheet.getItems().getFirst().getId(), itemCouponSnapshot, pointPolicy);
        }

        if (cartCouponSnapshot != null) {
            orderSheet.applyCartCoupon(cartCouponSnapshot, pointPolicy);
        }

        if (usedPoint != null) {
            orderSheet.applyPoints(usedPoint, pointPolicy);
        }

        return orderSheet;
    }

    private List<CreateOrderSheetItemContext> createDefaultOrderSheetItems() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = List.of(ProductOptionSnapshot.of("사이즈", "XL"));
        CreateOrderSheetItemContext itemContext = CreateOrderSheetItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .quantity(3)
                .optionSnapshots(options)
                .build();

        return List.of(itemContext);
    }
}
