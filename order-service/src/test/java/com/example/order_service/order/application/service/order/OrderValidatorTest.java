package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderValidatorTest {

    private final OrderValidator orderValidator = new OrderValidator();
    private final OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    private final PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(properties);

    @Test
    @DisplayName("주문 상품 결과에 누락된 상품이 있으면 예외가 발생한다")
    void validateOrderProduct_missing_product() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        OrderSheetItem item = orderSheet.getItems().getFirst();
        OrderProductResult.OrderProductDetail info = OrderProductResult.OrderProductDetail.builder()
                .productSnapshot(item.getProductSnapshot())
                .status(OrderProductStatus.ON_SALE)
                .stock(100)
                .priceSnapshot(item.getPriceSnapshot())
                .options(item.getOptionSnapshots())
                .build();
        OrderProductResult productList = OrderProductResult.builder()
                .products(List.of(info))
                .build();
        OrderCouponResult.Calculate couponResult = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 불가한 상품이 있으면 예외가 발생한다")
    void validateOrderProduct_unorderable_product() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        List<OrderProductResult.OrderProductDetail> infos = orderSheet.getItems().stream()
                .map(item -> OrderProductResult.OrderProductDetail.builder()
                        .productSnapshot(item.getProductSnapshot())
                        .status(OrderProductStatus.STOP_SALE)
                        .stock(item.getQuantity() + 100)
                        .priceSnapshot(item.getPriceSnapshot())
                        .options(item.getOptionSnapshots())
                        .build()).toList();
        OrderProductResult productList = OrderProductResult.builder()
                .products(infos)
                .build();
        OrderCouponResult.Calculate couponResult = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
    }

    @Test
    @DisplayName("주문 불가한 상품이 있으면 예외가 발생한다")
    void validateOrderProduct_insufficient_stock() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        List<OrderProductResult.OrderProductDetail> infos = orderSheet.getItems().stream()
                .map(item -> OrderProductResult.OrderProductDetail.builder()
                        .productSnapshot(item.getProductSnapshot())
                        .status(OrderProductStatus.ON_SALE)
                        .stock(item.getQuantity() - 1)
                        .priceSnapshot(item.getPriceSnapshot())
                        .options(item.getOptionSnapshots())
                        .build()).toList();
        OrderProductResult productList = OrderProductResult.builder()
                .products(infos)
                .build();
        OrderCouponResult.Calculate couponResult = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("주문서의 주문 상품 가격과 상품 결과의 판매가격이 다르면 예외가 발생한다")
    void validateOrderProduct() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        List<OrderProductResult.OrderProductDetail> infos = orderSheet.getItems().stream()
                .map(item -> {
                    ProductPriceSnapshot resultPrice = ProductPriceSnapshot.of(Money.wons(20000L), 10,
                            Money.wons(2000L), Money.wons(18000L));
                    return OrderProductResult.OrderProductDetail.builder()
                            .productSnapshot(item.getProductSnapshot())
                            .status(OrderProductStatus.ON_SALE)
                            .stock(item.getQuantity() + 100)
                            .priceSnapshot(resultPrice)
                            .options(item.getOptionSnapshots())
                            .build();
                }).toList();
        OrderProductResult productList = OrderProductResult.builder()
                .products(infos)
                .build();
        OrderCouponResult.Calculate couponResult = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.PRODUCT_PRICE_CHANGE);
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 가격이 다른 경우 예외가 발생한다")
    void validateOrderCartCoupon() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        OrderProductResult productResult = createValidProductList(orderSheet);
        List<OrderCouponResult.ItemCoupon> itemCoupons = orderSheet.getItems().stream().map(item -> OrderCouponResult.ItemCoupon.builder()
                .productVariantId(item.getProductVariantId())
                .itemCoupon(item.getItemCouponSnapshot())
                .build()).toList();
//        OrderCouponResult.Calculate couponResult = OrderCouponResult.Calculate.builder()
//                .cartCoupon(CartCouponSnapshot.of(orderSheet.getCartCoupon().getCartCouponId(),
//                        orderSheet.getCartCoupon().getName(), orderSheet.getCartCoupon().getDiscountAmount().add(Money.wons(1000L))))
//                .itemCoupons(itemCoupons)
//                .build();
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
//        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productResult, couponResult, userPoint, pointUsagePolicy))
//                .isInstanceOf(BusinessException.class)
//                .extracting("errorCode")
//                .isEqualTo(OrderErrorCode.CART_COUPON_DISCOUNT_CHANGE);
    }

    @Test
    @DisplayName("상품 쿠폰 할인 가격이 다른 경우 예외가 발생한다")
    void validateOrderItemCoupon() {
        OrderSheet orderSheet = createOrderSheet();
        OrderProductResult productResult = createValidProductList(orderSheet);
        OrderCouponResult.Calculate couponResult = OrderCouponResult.Calculate.builder()
                .cartCoupon(orderSheet.getCartCoupon())
//                .itemCoupons(invalidItemCoupons)
                .build();
        OrderUserResult.UserPoint userPoint = createValidUserPoint(orderSheet);
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productResult, couponResult, userPoint, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ITEM_COUPON_DISCOUNT_CHANGE);
    }

    @Test
    @DisplayName("주문서 포인트가 사용 가능 포인트를 넘는다면 예외가 발생한다")
    void validateOrderPoints() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        OrderProductResult productList = createValidProductList(orderSheet);
        OrderCouponResult.Calculate calculate = createValidCouponResult(orderSheet);
        OrderUserResult.UserPoint pointResult = OrderUserResult.UserPoint.builder()
                .userId(orderSheet.getOrderer().getUserId())
                .ownedPoints(Money.wons(500L))
                .build();
        //when
        //then
        assertThatThrownBy(() -> orderValidator.validate(orderSheet, productList, calculate, pointResult, pointUsagePolicy))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.POINTS_DISCOUNT_CHANGE);
    }

    private OrderProductResult createValidProductList(OrderSheet orderSheet) {
        List<OrderProductResult.OrderProductDetail> infos = orderSheet.getItems().stream().map(item ->
                        OrderProductResult.OrderProductDetail.builder()
                                .productSnapshot(item.getProductSnapshot())
                                .status(OrderProductStatus.ON_SALE)
                                .stock(item.getQuantity() + 100)
                                .priceSnapshot(item.getPriceSnapshot())
                                .options(item.getOptionSnapshots()).build())
                .toList();
        return OrderProductResult.builder()
                .products(infos)
                .build();
    }

    private OrderCouponResult.Calculate createValidCouponResult(OrderSheet orderSheet) {
        List<OrderCouponResult.ItemCoupon> validItemCoupons = orderSheet.getItems().stream()
                .map(item -> OrderCouponResult.ItemCoupon.builder()
                        .productVariantId(item.getProductVariantId())
                        .itemCoupon(item.getItemCouponSnapshot())
                        .build()).toList();

        return OrderCouponResult.Calculate.builder()
                .cartCoupon(orderSheet.getCartCoupon())
                .itemCoupons(validItemCoupons)
                .build();
    }

    private OrderUserResult.UserPoint createValidUserPoint(OrderSheet orderSheet) {
        return OrderUserResult.UserPoint.builder()
                .userId(orderSheet.getOrderer().getUserId())
                .ownedPoints(orderSheet.getUsedPoints().add(Money.wons(10000L)))
                .build();
    }

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product1 = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductSnapshot product2 = ProductSnapshot.of(1L, 2L, "PROD1-XL-RED", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options1 = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        List<ProductOptionSnapshot> options2 = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "RED")
        );
        OrderSheetItem sheetItem1 = OrderSheetItem.create(product1, price, 1, options1);
        OrderSheetItem sheetItem2 = OrderSheetItem.create(product2, price, 5, options2);
        return OrderSheet.create(orderer,  List.of(sheetItem1, sheetItem2), LocalDateTime.now().plusMinutes(30));
    }
}
