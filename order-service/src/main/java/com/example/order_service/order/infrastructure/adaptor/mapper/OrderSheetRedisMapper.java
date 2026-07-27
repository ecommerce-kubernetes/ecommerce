package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.infrastructure.adaptor.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class OrderSheetRedisMapper {

    public OrderSheetRedisEntity toEntity(OrderSheet orderSheet) {
        OrdererRedisEntity ordererRedisEntity = mapToOrdererRedisEntity(orderSheet.getOrderer());
        ShippingAddressRedisEntity shippingAddressRedisEntity = mapToShippingAddressRedisEntity(orderSheet.getShippingAddress());
        CartCouponRedisEntity cartCouponRedisEntity = mapToCartCouponRedisEntity(orderSheet.getCartCoupon());
        List<OrderSheetItemRedisEntity> orderSheetItemRedisEntities = mapToOrderSheetItemRedisEntities(orderSheet.getItems());
        return OrderSheetRedisEntity.builder()
                .id(orderSheet.getId())
                .orderer(ordererRedisEntity)
                .shippingAddress(shippingAddressRedisEntity)
                .items(orderSheetItemRedisEntities)
                .cartCoupon(cartCouponRedisEntity)
                .usedPoints(orderSheet.getUsedPoints().longValue())
                .expiresAt(orderSheet.getExpiresAt().toString())
                .build();
    }

    private OrdererRedisEntity mapToOrdererRedisEntity(Orderer orderer) {
        return OrdererRedisEntity.builder()
                .userId(orderer.getUserId())
                .userName(orderer.getUserName())
                .phoneNumber(orderer.getPhoneNumber())
                .build();
    }

    private ShippingAddressRedisEntity mapToShippingAddressRedisEntity(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }
        return ShippingAddressRedisEntity.builder()
                .receiverName(shippingAddress.getReceiverName())
                .receiverPhone(shippingAddress.getReceiverPhone())
                .zipCode(shippingAddress.getZipCode())
                .address(shippingAddress.getAddress())
                .addressDetail(shippingAddress.getAddressDetail())
                .build();
    }

    private OrderSheetItemRedisEntity mapToOrderSheetItemRedisEntity(OrderSheetItem orderSheetItem) {
        ProductSnapshotRedisEntity productSnapshotRedisEntity = mapToProductSnapshotRedisEntity(orderSheetItem.getProductSnapshot());
        ProductPriceSnapshotRedisEntity productPriceSnapshotRedisEntity = mapToProductPriceSnapshotRedisEntity(orderSheetItem.getPriceSnapshot());
        ItemCouponSnapshotRedisEntity itemCouponSnapshotRedisEntity = mapToItemCouponSnapshotRedisEntity(orderSheetItem.getItemCouponSnapshot());
        List<ProductOptionSnapshotRedisEntity> productOptionSnapshotRedisEntities = mapToProductOptionSnapshotRedisEntities(orderSheetItem.getOptionSnapshots());
        return OrderSheetItemRedisEntity.builder()
                .id(orderSheetItem.getId())
                .productSnapshot(productSnapshotRedisEntity)
                .priceSnapshot(productPriceSnapshotRedisEntity)
                .itemCouponSnapshot(itemCouponSnapshotRedisEntity)
                .quantity(orderSheetItem.getQuantity())
                .optionSnapshots(productOptionSnapshotRedisEntities)
                .build();
    }

    private List<OrderSheetItemRedisEntity> mapToOrderSheetItemRedisEntities(List<OrderSheetItem> orderSheetItems) {
        return orderSheetItems.stream().map(this::mapToOrderSheetItemRedisEntity).toList();
    }

    private ProductSnapshotRedisEntity mapToProductSnapshotRedisEntity(ProductSnapshot productSnapshot) {
        return ProductSnapshotRedisEntity.builder()
                .productId(productSnapshot.getProductId())
                .productVariantId(productSnapshot.getProductVariantId())
                .sku(productSnapshot.getSku())
                .productName(productSnapshot.getProductName())
                .thumbnail(productSnapshot.getThumbnail())
                .build();
    }

    private ProductOptionSnapshotRedisEntity mapToProductOptionSnapshotRedisEntity(ProductOptionSnapshot productOptionSnapshot) {
        return ProductOptionSnapshotRedisEntity.builder()
                .optionTypeName(productOptionSnapshot.getOptionTypeName())
                .optionValueName(productOptionSnapshot.getOptionValueName())
                .build();
    }

    private List<ProductOptionSnapshotRedisEntity> mapToProductOptionSnapshotRedisEntities(List<ProductOptionSnapshot> productOptionSnapshots) {
        if (productOptionSnapshots == null || productOptionSnapshots.isEmpty()) {
            return Collections.emptyList();
        }
        return productOptionSnapshots.stream().map(this::mapToProductOptionSnapshotRedisEntity).toList();
    }

    private ProductPriceSnapshotRedisEntity mapToProductPriceSnapshotRedisEntity(ProductPriceSnapshot priceSnapshot) {
        return ProductPriceSnapshotRedisEntity.builder()
                .originalPrice(priceSnapshot.getOriginalPrice().longValue())
                .discountRate(priceSnapshot.getDiscountRate())
                .discountAmount(priceSnapshot.getDiscountAmount().longValue())
                .discountedPrice(priceSnapshot.getDiscountedPrice().longValue())
                .build();
    }

    private ItemCouponSnapshotRedisEntity mapToItemCouponSnapshotRedisEntity(ItemCouponSnapshot itemCouponSnapshot) {
        if (itemCouponSnapshot == null) {
            return null;
        }

        ItemCouponSnapshotRedisEntity.ItemCouponSnapshotRedisEntityBuilder builder = ItemCouponSnapshotRedisEntity.builder()
                .itemCouponId(itemCouponSnapshot.getItemCouponId())
                .name(itemCouponSnapshot.getName())
                .applyQuantityLimit(itemCouponSnapshot.getApplyQuantityLimit());

        if (itemCouponSnapshot.getDiscountPolicy() instanceof FixedCouponDiscountPolicy fixedPolicy) {
            builder.policyType("FIXED")
                    .fixedDiscountAmount(fixedPolicy.getDiscountAmount().longValue());
        } else if (itemCouponSnapshot.getDiscountPolicy() instanceof RateCouponDiscountPolicy ratePolicy) {
            builder.policyType("RATE")
                    .discountRate(ratePolicy.getDiscountRate())
                    .maxDiscountAmount(ratePolicy.getMaxDiscountAmount().longValue());
        }

        return builder.build();
    }

    private CartCouponRedisEntity mapToCartCouponRedisEntity(CartCouponSnapshot cartCouponSnapshot) {
        if (cartCouponSnapshot == null) {
            return null;
        }

        CartCouponRedisEntity.CartCouponRedisEntityBuilder builder = CartCouponRedisEntity.builder()
                .cartCouponId(cartCouponSnapshot.getCartCouponId())
                .name(cartCouponSnapshot.getName())
                .minimumPaymentAmount(cartCouponSnapshot.getMinimumPaymentAmount().longValue());

        if (cartCouponSnapshot.getDiscountPolicy() instanceof FixedCouponDiscountPolicy fixedPolicy) {
            builder.policyType("FIXED")
                    .fixedDiscountAmount(fixedPolicy.getDiscountAmount().longValue());
        } else if (cartCouponSnapshot.getDiscountPolicy() instanceof RateCouponDiscountPolicy ratePolicy) {
            builder.policyType("RATE")
                    .discountRate(ratePolicy.getDiscountRate())
                    .maxDiscountAmount(ratePolicy.getMaxDiscountAmount().longValue());
        }

        return builder.build();
    }

    public OrderSheet toDomain(OrderSheetRedisEntity entity) {
        Orderer orderer = mapToOrderer(entity.getOrderer());
        ShippingAddress shippingAddress = mapToShippingAddress(entity.getShippingAddress());
        CartCouponSnapshot cartCouponSnapshot = mapToCartCouponSnapshot(entity.getCartCoupon());
        List<OrderSheetItem> orderSheetItems = mapToOrderSheetItems(entity.getItems());
        return OrderSheet.reconstitute()
                .id(entity.getId())
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(orderSheetItems)
                .cartCoupon(cartCouponSnapshot)
                .usedPoints(Money.wons(entity.getUsedPoints()))
                .expiresAt(LocalDateTime.parse(entity.getExpiresAt()))
                .build();
    }

    private Orderer mapToOrderer(OrdererRedisEntity entity) {
        return Orderer.of(entity.getUserId(), entity.getUserName(), entity.getPhoneNumber());
    }

    private ShippingAddress mapToShippingAddress(ShippingAddressRedisEntity entity) {
        if (entity == null) {
            return null;
        }
        return ShippingAddress.of(entity.getReceiverName(), entity.getReceiverPhone(), entity.getZipCode(), entity.getAddress(), entity.getAddressDetail());
    }

    private List<OrderSheetItem> mapToOrderSheetItems(List<OrderSheetItemRedisEntity> entities) {
        return entities.stream().map(this::mapToOrderSheetItem).toList();
    }

    private OrderSheetItem mapToOrderSheetItem(OrderSheetItemRedisEntity entity) {
        ProductSnapshot productSnapshot = mapToProductSnapshot(entity.getProductSnapshot());
        ProductPriceSnapshot priceSnapshot = mapToProductPriceSnapshot(entity.getPriceSnapshot());
        ItemCouponSnapshot itemCouponSnapshot = mapToItemCouponSnapshot(entity.getItemCouponSnapshot());
        List<ProductOptionSnapshot> optionSnapshots = mapToProductOptionSnapshots(entity.getOptionSnapshots());
        return OrderSheetItem.reconstitute()
                .id(entity.getId())
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .itemCoupon(itemCouponSnapshot)
                .quantity(entity.getQuantity())
                .optionSnapshots(optionSnapshots)
                .build();
    }

    private ProductSnapshot mapToProductSnapshot(ProductSnapshotRedisEntity entity) {
        return ProductSnapshot.of(entity.getProductId(), entity.getProductVariantId(), entity.getSku(), entity.getProductName(), entity.getThumbnail());
    }

    private ProductPriceSnapshot mapToProductPriceSnapshot(ProductPriceSnapshotRedisEntity entity) {
        return ProductPriceSnapshot.of(
                Money.wons(entity.getOriginalPrice()),
                entity.getDiscountRate(),
                Money.wons(entity.getDiscountAmount()),
                Money.wons(entity.getDiscountedPrice())
        );
    }

    private List<ProductOptionSnapshot> mapToProductOptionSnapshots(List<ProductOptionSnapshotRedisEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::mapToProductOptionSnapshot).toList();
    }

    private ProductOptionSnapshot mapToProductOptionSnapshot(ProductOptionSnapshotRedisEntity entity) {
        return ProductOptionSnapshot.of(entity.getOptionTypeName(), entity.getOptionValueName());
    }

    private CartCouponSnapshot mapToCartCouponSnapshot(CartCouponRedisEntity entity) {
        if (entity == null) {
            return null;
        }
        CouponDiscountPolicy policy = switch (entity.getPolicyType()) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(entity.getFixedDiscountAmount()));
            case "RATE" -> new RateCouponDiscountPolicy(entity.getDiscountRate(), Money.wons(entity.getMaxDiscountAmount()));
            default -> throw new IllegalArgumentException("알 수 없는 쿠폰 정책 타입입니다.");
        };

        return CartCouponSnapshot.of(
                entity.getCartCouponId(),
                entity.getName(),
                policy,
                Money.wons(entity.getMinimumPaymentAmount()));
    }

    private ItemCouponSnapshot mapToItemCouponSnapshot(ItemCouponSnapshotRedisEntity entity) {
        if (entity == null) {
            return null;
        }

        CouponDiscountPolicy policy = switch (entity.getPolicyType()) {
            case "FIXED" -> new FixedCouponDiscountPolicy(Money.wons(entity.getFixedDiscountAmount()));
            case "RATE" -> new RateCouponDiscountPolicy(entity.getDiscountRate(), Money.wons(entity.getMaxDiscountAmount()));
            default -> throw new IllegalArgumentException("알 수 없는 쿠폰 정책 타입입니다.");
        };

        return ItemCouponSnapshot.of(
                entity.getItemCouponId(),
                entity.getName(),
                policy,
                entity.getApplyQuantityLimit()
        );
    }
}
