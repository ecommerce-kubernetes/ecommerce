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
        List<Long> variantIds = command.items().stream().map(OrderSheetCommand.OrderItem::productVariantId).toList();
        List<OrderSheetProductResult.Info> products = orderSheetProductService.getProducts(variantIds);
        Map<Long, Integer> quantityMap = command.items().stream().collect(Collectors.toMap(OrderSheetCommand.OrderItem::productVariantId, OrderSheetCommand.OrderItem::quantity));
        String sheetId = UUID.randomUUID().toString();
        List<OrderSheetItem> orderSheetItems = products.stream().map(product -> {
            OrderSheetItemPriceSnapshot itemPrice = OrderSheetItemPriceSnapshot.of(product.originalPrice(),
                    product.discountRate(), product.discountAmount(), product.discountedPrice());
            OrderSheetItemProductSnapshot itemInfo = OrderSheetItemProductSnapshot.of(product.productId(),
                    product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            List<OrderSheetItemOptionSnapshot> options = product.options().stream().map(option ->
                    OrderSheetItemOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
            return OrderSheetItem.create(itemInfo, itemPrice, quantityMap.get(product.productVariantId()), options);
        }).toList();
        OrderSheet orderSheet = OrderSheet.create(sheetId, orderSheetItems, LocalDateTime.now());
        repository.save(orderSheet);
        return OrderSheetResult.Default.from(orderSheet);
    }
}
