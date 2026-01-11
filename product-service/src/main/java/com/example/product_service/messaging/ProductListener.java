package com.example.product_service.messaging;

import com.example.common.DeductedProduct;
import com.example.common.FailedEvent;
import com.example.common.OrderCreatedEvent;
import com.example.common.ProductStockDeductedEvent;
import com.example.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

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
        List<DeductedProduct> orderProducts = event.getDeductedProducts();
        Map<Long, Integer> reductionMap = orderProducts.stream().collect(Collectors.toMap(DeductedProduct::getProductVariantId, DeductedProduct::getQuantity));
        try{
            Map<Long, Integer> resultMap = productVariantService.inventoryReductionById(reductionMap);
            List<DeductedProduct> deductedList = resultMap.entrySet().stream().map(entry -> new DeductedProduct(entry.getKey(), entry.getValue())).toList();
            kafkaTemplate.send(STOCK_DEDUCTED, new ProductStockDeductedEvent(event.getOrderId(), deductedList));
            log.info("adsfasdfadsf");
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
