package com.example.order_service.ordersheet.application;

import com.example.order_service.api.common.exception.business.BusinessException;
import com.example.order_service.api.common.exception.business.code.OrderSheetErrorCode;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.dto.result.ProductStatus;
import com.example.order_service.ordersheet.domain.OrderSheet;
import com.example.order_service.ordersheet.domain.OrderSheetItem;
import com.example.order_service.ordersheet.domain.OrderSheetRepository;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemProductSnapshot;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderSheetProductService orderSheetProductService;
    private final OrderSheetRepository repository;

    public OrderSheetResult.Default createOrderSheet(OrderSheetCommand.Create command) {
        // 주문 상품 ID 추출
        List<Long> variantIds = command.items().stream().map(OrderSheetCommand.OrderItem::productVariantId).toList();
        // 상품 정보 조회
        List<OrderSheetProductResult.Info> products = orderSheetProductService.getProducts(variantIds);
        Map<Long, Integer> quantityMap = createQuantityMap(command);
        String sheetId = generateSheetId();
        // orderSheet 데이터 생성 및 저장
        List<OrderSheetItem> orderSheetItems = mapToDomainItems(products, quantityMap);
        OrderSheet orderSheet = OrderSheet.create(sheetId, orderSheetItems, LocalDateTime.now());
        // ttl
        long ttlMinutes = orderSheetProperties.ttlMinutes();
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(ttlMinutes));
        return OrderSheetResult.Default.from(savedOrderSheet);
    }

    //상품 아이템 도메인 매핑
    private List<OrderSheetItem> mapToDomainItems(List<OrderSheetProductResult.Info> products, Map<Long, Integer> quantityMap) {
        return products.stream().map(product -> {
            Integer quantity = quantityMap.get(product.productVariantId());
            validateAvailableToOrder(product, quantity);
            OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(),
                    product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(),
                    product.discountRate(), product.discountAmount(), product.discountedPrice());
            List<OrderSheetItemOptionSnapshot> options = mapToOptionSnapshots(product.options());
            return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, options);
        }).toList();
    }

    //주문 가능한 상품인지 검증
    private void validateAvailableToOrder(OrderSheetProductResult.Info product, int quantity) {
        // 판매중인 상품이 아니면 주문할 수 없음
        if (product.status() != ProductStatus.ON_SALE) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_NOT_ON_SALE);
        }
        // 주문 수량이 상품 재고보다 많은 경우 주문할 수 없음
        if (product.stock() < quantity) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_INSUFFICIENT_STOCK);
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
    private String generateSheetId() {
        return UUID.randomUUID().toString();
    }

    private Map<Long, Integer> createQuantityMap(OrderSheetCommand.Create command) {
        return command.items().stream().collect(Collectors
                .toMap(OrderSheetCommand.OrderItem::productVariantId, OrderSheetCommand.OrderItem::quantity));
    }
}
