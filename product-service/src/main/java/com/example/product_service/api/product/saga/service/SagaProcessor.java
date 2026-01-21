package com.example.product_service.api.product.saga.service;

import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.product.saga.producer.SagaEventProducer;
import com.example.product_service.api.product.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SagaProcessor {
    private final VariantService variantService;
    private final SagaEventProducer sagaEventProducer;

    public void productSagaProcess(ProductSagaCommand command) {
        if (command.getType().equals(ProductCommandType.DEDUCT_STOCK)) {
            Map<Long, Integer> deductMap = command.getItems().stream().collect(Collectors.toMap(Item::getProductVariantId, Item::getQuantity));
            variantService.deductVariantStock(deductMap);
            sagaEventProducer.requestStockDeductionSuccess(command.getSagaId(), command.getOrderNo());
        }
    }
}
