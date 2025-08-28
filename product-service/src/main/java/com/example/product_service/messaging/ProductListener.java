package com.example.product_service.messaging;

import com.example.common.OrderCreatedEvent;
import com.example.common.Product;
import com.example.product_service.service.ProductVariantService;
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

    @KafkaListener(topics = ORDER_CREATED)
    public void inventoryReductionListener(@Payload OrderCreatedEvent event){
        List<Product> productList = event.getProductList();
        Map<Long, Integer> collect = productList.stream().collect(Collectors.toMap(Product::getProductVariantId, Product::getStock));
    }
}
