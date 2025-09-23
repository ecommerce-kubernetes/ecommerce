package com.example.product_service.messaging;

import com.example.common.*;
import com.example.product_service.service.ProductVariantService;
import com.example.product_service.service.dto.InventoryReductionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductListener {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductVariantService productVariantService;
    private static final String ORDER_CREATED = "order.created";
    private static final String STOCK_DEDUCTED = "product.stock.deducted";
    private static final String STOCK_DEDUCTED_FAIL = "product.stock.failed";
    private static final String STOCK_RESTORE = "product.stock.restore";

    @KafkaListener(topics = ORDER_CREATED)
    public void inventoryReductionListener(@Payload OrderCreatedEvent event){
        List<OrderProduct> orderProducts = event.getOrderProductList();
        Map<Long, Integer> reductionMap = orderProducts.stream().collect(Collectors.toMap(OrderProduct::getProductVariantId, OrderProduct::getStock));
        try{
            List<InventoryReductionItem> inventoryReductionItems = productVariantService.inventoryReductionById(reductionMap);
            List<DeductedProduct> reductionStat = inventoryReductionItems.stream()
                    .map(item ->
                            new DeductedProduct(item.getProductId(), item.getProductVariantId(), item.getProductName(), item.getThumbnail(),
                                    new PriceInfo(item.getPrice(), item.getDiscountRate(), item.getDiscountAmount(), item.getFinalPrice()),
                                    item.getStock(), List.of(new ItemOption("색상", "RED")))).toList();
            kafkaTemplate.send(STOCK_DEDUCTED, new ProductStockDeductedEvent(event.getOrderId(), reductionStat));
        } catch (Exception e){
            kafkaTemplate.send(STOCK_DEDUCTED_FAIL, new FailedEvent(event.getOrderId(), e.getMessage()));
        }
    }

    @KafkaListener(topics = STOCK_RESTORE)
    public void inventoryRestoreListener(@Payload ProductStockDeductedEvent event){
        List<DeductedProduct> productList = event.getDeductedProducts();
        Map<Long, Integer> restoreMap = productList.stream().collect(Collectors.toMap(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity));
        productVariantService.inventoryRestorationById(restoreMap);
    }

}
