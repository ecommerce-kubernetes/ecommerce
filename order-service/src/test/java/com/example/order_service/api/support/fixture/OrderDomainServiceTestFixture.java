package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.vo.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;

import java.util.List;

public class OrderDomainServiceTestFixture {
    public static final Long USER_ID = 1L;
    public static final String ADDRESS = "서울시 테헤란로 123";
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    public static final Long PROD1_ID = 1L;
    public static final String PROD1_NAME = "상품1";
    public static final Long PROD1_PRICE = 3000L;
    public static final int PROD1_QTY = 3;

    public static final Long PROD2_ID = 2L;
    public static final String PROD2_NAME = "상품2";
    public static final Long PROD2_PRICE = 5000L;
    public static final int PROD2_QTY = 5;

    public static final int DISCOUNT_RATE = 10;
    public static final Long COUPON_DISCOUNT = 1000L;
    public static final Long USE_POINT = 1000L;

    public static final Long PROD1_LINE_TOTAL = (PROD1_PRICE * (100 - DISCOUNT_RATE) / 100) * PROD1_QTY;
    public static final Long PROD2_LINE_TOTAL = (PROD2_PRICE * (100 - DISCOUNT_RATE) / 100) * PROD2_QTY;

    public static final Long TOTAL_ORIGIN_PRICE = (PROD1_PRICE * PROD1_QTY) + (PROD2_PRICE * PROD2_QTY);
    public static final Long TOTAL_PROD_DISCOUNT = TOTAL_ORIGIN_PRICE * DISCOUNT_RATE / 100;
    public static final Long FINAL_PRICE = TOTAL_ORIGIN_PRICE - TOTAL_PROD_DISCOUNT - COUPON_DISCOUNT - USE_POINT;


    private static OrderItemCreationContext createOrderItemCommand(Long productId, String productName, Long price, int quantity,
                                                                   Long lineTotal) {
//        return OrderItemCreationContext.builder()
//                .productId(productId)
//                .productVariantId(productId)
//                .productName(productName)
//                .thumbnailUrl("http://thumbnail.jpg")
//                .unitPrice(
//                        OrderItemCreationContext.UnitPrice.builder()
//                                .originalPrice(price)
//                                .discountRate(DISCOUNT_RATE)
//                                .discountAmount(price/DISCOUNT_RATE)
//                                .discountedPrice(price - (price/DISCOUNT_RATE))
//                                .build())
//                .itemOptions(List.of())
//                .quantity(quantity)
//                .lineTotal(lineTotal)
//                .build();

        return null;
    }

    private static OrderItemCreationContext mockOrderItemContext(OrderedProduct orderedProduct, OrderItemPrice orderItemPrice, int quantity) {
        return OrderItemCreationContext
                .builder()
                .orderedProduct(orderedProduct)
                .orderItemPrice(orderItemPrice)
                .quantity(quantity)
                .lineTotal(orderItemPrice.getDiscountedPrice() * quantity)
                .itemOptions(List.of(OrderItemCreationContext.ItemOption.builder().optionTypeName("사이즈").optionValueName("XL").build()))
                .build();
    }
}
