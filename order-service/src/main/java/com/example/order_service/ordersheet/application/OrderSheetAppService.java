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
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.ProductStatus;
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

    public OrderSheetResult.Default createOrderSheet(OrderSheetCommand.Create command) {
        List<OrderSheetProductResult.Info> orderProducts = getOrderProducts(command);
        OrderSheetCouponResult.Calculate appliedCoupons = getAppliedCoupons(command, orderProducts);
        return null;
    }

    private List<OrderSheetProductResult.Info> getOrderProducts(OrderSheetCommand.Create command) {
        List<Long> variantIds = command.toProductVariantIds();
        Map<Long, Integer> quantityMap = command.toQuantityMap();
        List<OrderSheetProductResult.Info> products = orderSheetProductGateway.getProducts(variantIds);
        validateProductsForOrder(products, quantityMap);
        return products;
    }

    private OrderSheetCouponResult.Calculate getAppliedCoupons(OrderSheetCommand.Create command, List<OrderSheetProductResult.Info> products) {
        boolean couponUsed = isCouponUsed(command);
        if (!couponUsed) {
            return null;
        }
        Map<Long, OrderSheetProductResult.Info> productMap = products.stream().collect(Collectors.toMap(OrderSheetProductResult.Info::productVariantId, Function.identity()));
        Map<Long, Long> couponMap = command.toCouponMap();
        List<OrderSheetCommand.AppliedCouponItem> appliedCouponItems = new ArrayList<>();
        for(OrderSheetCommand.OrderItem item: command.items()) {
            OrderSheetCommand.AppliedCouponItem coupon = OrderSheetCommand.AppliedCouponItem.builder()
                    .productVariantId(item.productVariantId())
                    .discountedPrice(productMap.get(item.productVariantId()).discountedPrice())
                    .quantity(item.quantity())
                    .itemCouponId(couponMap.get(item.productVariantId()))
                    .build();
            appliedCouponItems.add(coupon);
        }

        OrderSheetCommand.CouponCalculate couponCommand = OrderSheetCommand.CouponCalculate.builder()
                .userId(command.userId())
                .cartCouponId(command.cartCouponId())
                .items(appliedCouponItems)
                .build();

        return orderSheetCouponGateway.calculate(couponCommand);
    }

    private boolean isCouponUsed(OrderSheetCommand.Create command) {
        if (command.cartCouponId() == null && command.itemCoupons().isEmpty()) {
            return false;
        }
        return true;
    }

    public OrderSheetResult.Detail getOrderSheet(String sheetId, Long userId) {
        OrderSheet orderSheet = findByOrThrow(sheetId);
        // 주문서 생성 유저와 조회 유저가 일치하지 않음
        if (!orderSheet.getUserId().equals(userId)) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_NO_PERMISSION);
        }
        return null;
    }

    private List<OrderSheetItem> mapToDomainItems(List<OrderSheetProductResult.Info> products, Map<Long, Integer> quantityMap) {
        return products.stream().map(product -> {
            String sheetItemId = generateId();
            Integer quantity = quantityMap.get(product.productVariantId());
            OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(),
                    product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(),
                    product.discountRate(), product.discountAmount(), product.discountedPrice());
            List<OrderSheetItemOptionSnapshot> options = mapToOptionSnapshots(product.options());
            return OrderSheetItem.create(sheetItemId, productSnapshot, priceSnapshot, quantity, options);
        }).toList();
    }

    //상품 아이템 도메인 매핑
    private void validateProductsForOrder(List<OrderSheetProductResult.Info> products, Map<Long, Integer> quantityMap) {
        if (products.size() != quantityMap.size()) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_NOT_FOUND);
        }

        for (OrderSheetProductResult.Info product : products) {
            Integer requestedQuantity = quantityMap.get(product.productVariantId());
            // 주문 불가 상품 검증
            if (product.status() != ProductStatus.ORDERABLE) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNORDERABLE);
            }
            //
            if (product.stock() < requestedQuantity){
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_INSUFFICIENT_STOCK);
            }
        }
    }

    //상품 옵션 도메인 매핑
    private List<OrderSheetItemOptionSnapshot> mapToOptionSnapshots(List<OrderSheetProductResult.Option> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream().map(option ->
                OrderSheetItemOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    // UUID 로 id 생성
    private String generateId() {
        return UUID.randomUUID().toString();
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
