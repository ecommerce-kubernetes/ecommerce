package com.example.product_service.messaging;

import com.example.common.FailedEvent;
import com.example.common.OrderCreatedEvent;
import com.example.common.Product;
import com.example.common.ProductStockDeductedEvent;
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

    @KafkaListener(topics = ORDER_CREATED)
    public void inventoryReductionListener(@Payload OrderCreatedEvent event){
        List<Product> productList = event.getProductList();
        Map<Long, Integer> reductionMap = productList.stream().collect(Collectors.toMap(Product::getProductVariantId, Product::getStock));
        try{
            List<InventoryReductionItem> inventoryReductionItems = productVariantService.inventoryReductionById(reductionMap);
            List<Product> reductionStat = inventoryReductionItems.stream()
                    .map(item ->
                            new Product(item.getProductVariantId(), item.getStock(), item.getDiscountPrice())).toList();
            kafkaTemplate.send(STOCK_DEDUCTED, new ProductStockDeductedEvent(event.getOrderId(), reductionStat));
        } catch (Exception e){
            kafkaTemplate.send(STOCK_DEDUCTED_FAIL, new FailedEvent(event.getOrderId(), e.getMessage()));
        }
    }


}
