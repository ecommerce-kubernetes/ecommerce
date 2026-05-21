package com.example.order_service.ordersheet.application;

import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class OrderSheetFactory {

    public OrderSheet createSheet(OrderSheetCommand.Create command, OrderSheetUserResult.Profile userResult,
                                  OrderSheetProductResult.ProductList productResult, OrderSheetCouponResult.Calculate couponResult, long ttlMinute) {
        String sheetId = generateId();
        Orderer orderer = createOrderer(userResult);
        ShippingAddress shippingAddress = createShippingAddress(userResult);
        List<OrderSheetItem> sheetItems = createItems(command, productResult, couponResult);
        OrderCouponSnapshot cartCoupon = createCartCoupon(couponResult.cartCoupon());
        return OrderSheet.create(sheetId, orderer, shippingAddress, sheetItems, cartCoupon, LocalDateTime.now(), ttlMinute);
    }

    public OrderCouponSnapshot createItemCouponSnapshot(OrderSheetCouponResult.Calculate appliedCoupons, Long variantId) {
        Map<Long, OrderSheetCouponResult.ItemCoupon> itemCouponMap = appliedCoupons.toItemCouponMap();
        return createItemCoupon(itemCouponMap.get(variantId));
    }

    public ShippingAddress createShippingAddress(OrderSheetCommand.UpdateShippingAddress command) {
        return ShippingAddress.of(command.receiverName(), command.receiverPhone(), command.zipCode(), command.address(),
                command.addressDetail());
    }

    private List<OrderSheetItem> createItems(OrderSheetCommand.Create command, OrderSheetProductResult.ProductList productResult, OrderSheetCouponResult.Calculate couponResult) {
        Map<Long, OrderSheetProductResult.Info> productsMap = productResult.getProductsMap();
        Map<Long, OrderSheetCouponResult.ItemCoupon> itemCouponMap = couponResult.toItemCouponMap();
        return command.items().stream().map(item -> createItem(item, productsMap, itemCouponMap)).toList();
    }

    private OrderCouponSnapshot createCartCoupon(OrderSheetCouponResult.CartCoupon cartCoupon) {
        if (cartCoupon == null) {
            return OrderCouponSnapshot.empty();
        }
        return OrderCouponSnapshot.of(cartCoupon.couponId(), cartCoupon.couponName(), cartCoupon.discountAmount());
    }

    private OrderCouponSnapshot createItemCoupon(OrderSheetCouponResult.ItemCoupon itemCoupon) {
        if (itemCoupon == null) {
            return OrderCouponSnapshot.empty();
        }
        return OrderCouponSnapshot.of(itemCoupon.couponId(), itemCoupon.couponName(), itemCoupon.discountAmount());
    }

    private OrderSheetItem createItem(OrderSheetCommand.OrderItem command, Map<Long, OrderSheetProductResult.Info> productsMap,
                                      Map<Long, OrderSheetCouponResult.ItemCoupon> itemCouponMap) {
        Long orderedVariantId = command.productVariantId();
        String sheetItemId = generateId();
        OrderSheetProductResult.Info product = productsMap.get(orderedVariantId);
        OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(),
                product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
        OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(
                product.originalPrice(), product.discountRate(), product.discountAmount(), product.discountedPrice());
        List<OrderSheetItemOptionSnapshot> optionSnapshots = createOptions(product.options());
        OrderCouponSnapshot couponSnapshot = createItemCoupon(itemCouponMap.get(orderedVariantId));
        return OrderSheetItem.create(sheetItemId, productSnapshot, priceSnapshot, couponSnapshot, command.quantity(), optionSnapshots);
    }

    private List<OrderSheetItemOptionSnapshot> createOptions(List<OrderSheetProductResult.Option> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream().map(option ->
                OrderSheetItemOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    private Orderer createOrderer(OrderSheetUserResult.Profile profile) {
        return Orderer.of(profile.userId(), profile.userName(), profile.phoneNumber());
    }

    private ShippingAddress createShippingAddress(OrderSheetUserResult.Profile profile) {
        OrderSheetUserResult.ShippingAddress shippingAddress = profile.shippingAddress();
        return ShippingAddress.of(
                shippingAddress.receiverName(), shippingAddress.receiverPhone(),
                shippingAddress.zipCode(), shippingAddress.address(), shippingAddress.addressDetail()
        );
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
