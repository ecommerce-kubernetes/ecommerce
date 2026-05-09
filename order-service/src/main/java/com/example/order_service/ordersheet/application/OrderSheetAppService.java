package com.example.order_service.ordersheet.application;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetAppService {
    private final OrderSheetProperties orderSheetProperties;
    private final OrderSheetProductGateway orderSheetProductGateway;
    private final OrderSheetRepository repository;

    public OrderSheetResult.Default createOrderSheet(OrderSheetCommand.Create command) {
        // 주문 상품 정보 추출
        List<Long> variantIds = command.toProductVariantIds();
        Map<Long, Integer> quantityMap = command.toQuantityMap();
        // 상품 정보 조회
        List<OrderSheetProductResult.Info> products = orderSheetProductGateway.getProducts(variantIds);
        // 주문 상품 검증
        validateProductsForOrder(products, quantityMap);
        String sheetId = generateSheetId();
        // orderSheet 데이터 생성 및 저장
        List<OrderSheetItem> orderSheetItems = mapToDomainItems(products, quantityMap);
        OrderSheet orderSheet = OrderSheet.create(sheetId, orderSheetItems, LocalDateTime.now(), orderSheetProperties.ttlMinutes());
        // ttl
        OrderSheet savedOrderSheet = repository.save(orderSheet, Duration.ofMinutes(orderSheetProperties.ttlMinutes()));
        return OrderSheetResult.Default.from(savedOrderSheet);
    }

    private List<OrderSheetItem> mapToDomainItems(List<OrderSheetProductResult.Info> products, Map<Long, Integer> quantityMap) {
        return products.stream().map(product -> {
            Integer quantity = quantityMap.get(product.productVariantId());
            OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(),
                    product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(),
                    product.discountRate(), product.discountAmount(), product.discountedPrice());
            List<OrderSheetItemOptionSnapshot> options = mapToOptionSnapshots(product.options());
            return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, options);
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
    private String generateSheetId() {
        return UUID.randomUUID().toString();
    }

}
