package com.example.order_service.ordersheet.application;

import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.domain.OrderSheet;
import com.example.order_service.ordersheet.domain.OrderSheetItem;
import com.example.order_service.ordersheet.domain.OrderSheetRepository;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.vo.OrderSheetItemProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderSheetService {
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
        OrderSheet savedOrderSheet = repository.save(orderSheet);
        return OrderSheetResult.Default.from(savedOrderSheet);
    }

    //상품 아이템 도메인 매핑
    private List<OrderSheetItem> mapToDomainItems(List<OrderSheetProductResult.Info> products, Map<Long, Integer> quantityMap) {
        return products.stream().map(product -> {
            OrderSheetItemProductSnapshot productSnapshot = OrderSheetItemProductSnapshot.of(product.productId(),
                    product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            OrderSheetItemPriceSnapshot priceSnapshot = OrderSheetItemPriceSnapshot.of(product.originalPrice(),
                    product.discountRate(), product.discountAmount(), product.discountedPrice());
            List<OrderSheetItemOptionSnapshot> options = mapToOptionSnapshots(product.options());
            Integer quantity = quantityMap.get(product.productVariantId());
            return OrderSheetItem.create(productSnapshot, priceSnapshot, quantity, options);
        }).toList();
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
