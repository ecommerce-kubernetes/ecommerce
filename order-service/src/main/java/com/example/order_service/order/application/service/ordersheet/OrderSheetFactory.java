package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public OrderSheet createSheet(OrderSheetCommand.Create command, OrderUserResult.Profile userResult,
                                  OrderProductResult.ProductList productResult, OrderCouponResult.Calculate couponResult, long ttlMinute) {
        String sheetId = generateId();
        Orderer orderer = userResult.orderer();
        ShippingAddress shippingAddress = userResult.shippingAddress();
        List<OrderSheetItem> sheetItems = createItems(command, productResult, couponResult);
        CartCouponSnapshot cartCoupon = couponResult.cartCoupon() != null ? couponResult.cartCoupon() : CartCouponSnapshot.empty();
        return OrderSheet.create(orderer, sheetItems, LocalDateTime.now().plusMinutes(ttlMinute));
    }

    private List<OrderSheetItem> createItems(OrderSheetCommand.Create command, OrderProductResult.ProductList productResult, OrderCouponResult.Calculate couponResult) {
        Map<Long, OrderProductResult.Info> productsMap = productResult.getProductsMap();
        Map<Long, ItemCouponSnapshot> itemCouponMap = couponResult.toItemCouponMap();
        return command.items().stream().map(item -> createItem(item, productsMap, itemCouponMap)).toList();
    }

    private OrderSheetItem createItem(OrderSheetCommand.OrderItem command, Map<Long, OrderProductResult.Info> productsMap,
                                      Map<Long, ItemCouponSnapshot> itemCouponMap) {
        Long orderedVariantId = command.productVariantId();
        String sheetItemId = generateId();
        OrderProductResult.Info product = productsMap.get(orderedVariantId);
        ProductSnapshot productSnapshot = product.productSnapshot();
        ProductPriceSnapshot priceSnapshot = product.priceSnapshot();
        List<ProductOptionSnapshot> optionSnapshots = product.options();
//        ItemCouponSnapshot couponSnapshot = itemCouponMap.getOrDefault(orderedVariantId, ItemCouponSnapshot.empty());
        return OrderSheetItem.create(productSnapshot, priceSnapshot, command.quantity(), optionSnapshots);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
