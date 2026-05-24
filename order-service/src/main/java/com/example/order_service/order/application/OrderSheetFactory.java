package com.example.order_service.order.application;

import com.example.order_service.order.application.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.order.application.dto.result.OrderSheetProductResult;
import com.example.order_service.order.application.dto.result.OrderSheetUserResult;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 주문서(OrderSheet) 도메인 생성 팩토리
 * <p>
 * 주문 커맨드와 외부 도메인 응답 결과를 통해 주문서, 주문상품 도메인과 도메인의 VO를 생성을 담당
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Component
public class OrderSheetFactory {

    /**
     * 주문서 도메인 생성
     * <p>
     * 주문서 생성 커맨드와 유저, 쿠폰, 상품 결과를 사용하여 주문서(OrderSheet) 애그리거트 루트를 생성
     * </p>
     *
     * @param command       주문서 생성 커맨드
     * @param userResult    주문 유저 프로필
     * @param productResult 주문 상품 정보
     * @param couponResult  적용 쿠폰 정보
     * @param ttlMinute     주문서 만료 시간
     * @return 주문서 애그리거트 루트
     */
    public OrderSheet createSheet(OrderSheetCommand.Create command, OrderSheetUserResult.Profile userResult,
                                  OrderSheetProductResult.ProductList productResult, OrderSheetCouponResult.Calculate couponResult, long ttlMinute) {
        String sheetId = generateId();
        Orderer orderer = createOrderer(userResult);
        ShippingAddress shippingAddress = createShippingAddress(userResult);
        List<OrderSheetItem> sheetItems = createItems(command, productResult, couponResult);
        OrderCouponSnapshot cartCoupon = createCartCoupon(couponResult.cartCoupon());
        return OrderSheet.create(sheetId, orderer, shippingAddress, sheetItems, cartCoupon, LocalDateTime.now(), ttlMinute);
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

    private OrderCouponSnapshot createCartCoupon(OrderSheetCouponResult.CartCoupon cartCoupon) {
        if (cartCoupon == null) {
            return OrderCouponSnapshot.empty();
        }
        return OrderCouponSnapshot.of(cartCoupon.couponId(), cartCoupon.couponName(), cartCoupon.discountAmount());
    }

    private List<OrderSheetItem> createItems(OrderSheetCommand.Create command, OrderSheetProductResult.ProductList productResult, OrderSheetCouponResult.Calculate couponResult) {
        Map<Long, OrderSheetProductResult.Info> productsMap = productResult.getProductsMap();
        Map<Long, OrderSheetCouponResult.ItemCoupon> itemCouponMap = couponResult.toItemCouponMap();
        return command.items().stream().map(item -> createItem(item, productsMap, itemCouponMap)).toList();
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

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 상품 쿠폰 스냅샷(VO) 생성
     * <p>
     * 쿠폰 검증 결과와 상품 변형 아이디를 통해 상품 쿠폰 스냅샷 VO를 생성
     * </p>
     *
     * @param appliedCoupons 쿠폰 검증 결과
     * @param variantId      주문서 상품 아이디
     * @return 상품 쿠폰 스냅샷 VO
     */
    public OrderCouponSnapshot createItemCouponSnapshot(OrderSheetCouponResult.Calculate appliedCoupons, Long variantId) {
        Map<Long, OrderSheetCouponResult.ItemCoupon> itemCouponMap = appliedCoupons.toItemCouponMap();
        return createItemCoupon(itemCouponMap.get(variantId));
    }

    private OrderCouponSnapshot createItemCoupon(OrderSheetCouponResult.ItemCoupon itemCoupon) {
        if (itemCoupon == null) {
            return OrderCouponSnapshot.empty();
        }
        return OrderCouponSnapshot.of(itemCoupon.couponId(), itemCoupon.couponName(), itemCoupon.discountAmount());
    }

    /**
     * 장바구니 쿠폰 스냅샷(VO) 생성
     * <p>
     * 쿠폰 검증 결과를 통해 장바구니 쿠폰 스냅샷 VO를 생성
     * </p>
     *
     * @param coupon 쿠폰 검증 결과
     * @return 장바구니 쿠폰 VO
     */
    public OrderCouponSnapshot createCartCouponSnapshot(OrderSheetCouponResult.CartCoupon coupon) {
        if (coupon == null) {
            return OrderCouponSnapshot.empty();
        }
        return OrderCouponSnapshot.of(coupon.couponId(), coupon.couponName(), coupon.discountAmount());
    }

    /**
     * 배송 정보(VO) 생성
     * <p>
     * 주문서 배송 정보 변경 커맨드를 통해 배송 정보 VO를 생성
     * </p>
     *
     * @param command 배송 정보 수정 커맨드
     * @return 배송 정보 VO
     */
    public ShippingAddress createShippingAddress(OrderSheetCommand.UpdateShippingAddress command) {
        return ShippingAddress.of(command.receiverName(), command.receiverPhone(), command.zipCode(), command.address(),
                command.addressDetail());
    }
}
