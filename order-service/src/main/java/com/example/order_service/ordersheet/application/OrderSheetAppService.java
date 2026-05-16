package com.example.order_service.ordersheet.application;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.external.OrderSheetCouponGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetProductGateway;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.OrderCouponSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetAppService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderSheetProductGateway orderSheetProductGateway;
    private final OrderSheetCouponGateway orderSheetCouponGateway;
    private final OrderSheetRepository repository;

    public OrderSheetResult.Create createOrderSheet(OrderSheetCommand.Create command) {
        // 주문 상품 조회
        List<OrderSheetProductResult.Info> products = orderSheetProductGateway.getProducts(command.items());
        // 적용 쿠폰 조회
        OrderSheetCouponResult.Calculate appliedCoupons = getAppliedCoupons(command, products);
        // 주문서 아이템 생성
        List<OrderSheetItem> orderSheetItems = mapToOrderSheetItems(command, products, appliedCoupons);
        // 주문서 생성
        OrderSheet orderSheet = createOrderSheet(command, orderSheetItems, appliedCoupons.cartCoupon());
        // 주문서 저장
        OrderSheet save = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetResult.Create.from(save);
    }

    // 주문 시트 도메인 생성
    private OrderSheet createOrderSheet(OrderSheetCommand.Create command, List<OrderSheetItem> items, OrderSheetCouponResult.CartCoupon cartCoupon) {
        OrderCouponSnapshot cartCouponSnapshot = Optional.ofNullable(cartCoupon)
                .map(coupon -> OrderCouponSnapshot.of(coupon.couponId(), coupon.couponName(), coupon.discountAmount()))
                .orElseGet(OrderCouponSnapshot::empty);
        return OrderSheet.create(generateId(), command.userId(), items, cartCouponSnapshot, LocalDateTime.now(), orderSheetProperties.ttlMinutes());
    }

    //적용 쿠폰 조회
    private OrderSheetCouponResult.Calculate getAppliedCoupons(OrderSheetCommand.Create command, List<OrderSheetProductResult.Info> products) {
        if (!command.hasCoupons()) {
            return OrderSheetCouponResult.Calculate.empty();
        }
        Map<Long, OrderSheetProductResult.Info> productMap = products.stream()
                .collect(Collectors.toMap(OrderSheetProductResult.Info::productVariantId, Function.identity()));
        Map<Long, Long> couponMap = command.toCouponMap();
        OrderSheetCommand.CouponCalculate couponCommand = mapToCouponCommand(command, productMap, couponMap);
        return orderSheetCouponGateway.calculate(couponCommand);
    }

    //쿠폰 커맨드 매핑
    private OrderSheetCommand.CouponCalculate mapToCouponCommand(OrderSheetCommand.Create command, Map<Long, OrderSheetProductResult.Info> productMap,
                                                                 Map<Long, Long> couponMap) {
        List<OrderSheetCommand.AppliedCouponItem> appliedCouponItems = command.items().stream().map(item -> {
            OrderSheetProductResult.Info product = productMap.get(item.productVariantId());
            Long itemCouponId = couponMap.get(item.productVariantId());
            return OrderSheetCommand.AppliedCouponItem.of(
                    item.productVariantId(),
                    product.discountedPrice(),
                    item.quantity(),
                    itemCouponId
            );
        }).toList();
        return OrderSheetCommand.CouponCalculate.of(command.userId(), command.cartCouponId(), appliedCouponItems);
    }

    //주문 시트 아이템 매핑
    private List<OrderSheetItem> mapToOrderSheetItems(OrderSheetCommand.Create command, List<OrderSheetProductResult.Info> products, OrderSheetCouponResult.Calculate coupon) {
        Map<Long, OrderSheetProductResult.Info> productMap = products.stream().collect(Collectors.toMap(OrderSheetProductResult.Info::productVariantId, Function.identity()));
        Map<Long, OrderSheetCouponResult.ItemCoupon> couponMap = coupon.toItemCouponMap();
        return command.items().stream()
                .map(orderItem -> createOrderSheetItem(orderItem, productMap, couponMap)).toList();
    }

    //주문 시트 아이템 생성
    private OrderSheetItem createOrderSheetItem(OrderSheetCommand.OrderItem orderItem,
                                                Map<Long, OrderSheetProductResult.Info> productMap,
                                                Map<Long, OrderSheetCouponResult.ItemCoupon> couponMap) {
        OrderSheetProductResult.Info product = productMap.get(orderItem.productVariantId());
        OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(), product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
        OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(), product.discountRate(), product.discountAmount(), product.discountedPrice());
        List<OrderSheetItemOptionSnapshot> optionSnapshots = mapToOptionSnapshots(product.options());
        OrderCouponSnapshot couponSnapshot = Optional.ofNullable(couponMap.get(orderItem.productVariantId()))
                .map(itemCoupon -> OrderCouponSnapshot.of(itemCoupon.couponId(), itemCoupon.couponName(), itemCoupon.discountAmount()))
                .orElseGet(OrderCouponSnapshot::empty);
        String sheetItemId = generateId();
        return OrderSheetItem.create(sheetItemId, productSnapshot, priceSnapshot, couponSnapshot, orderItem.quantity(), optionSnapshots);
    }

    //상품 옵션 매핑
    private List<OrderSheetItemOptionSnapshot> mapToOptionSnapshots(List<OrderSheetProductResult.Option> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream().map(option ->
                OrderSheetItemOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public OrderSheetResult.Detail getOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = findByOrThrow(sheetId);
        // 주문서 생성 유저와 조회 유저가 일치하지 않음
        if (!orderSheet.getUserId().equals(userId)) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
        return null;
    }

    private OrderSheet findByOrThrow(String sheetId) {
        Optional<OrderSheet> sheetOptional = repository.findById(sheetId);
        if (sheetOptional.isPresent()) {
            return sheetOptional.get();
        } else {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NOT_FOUND);
        }
    }
}
