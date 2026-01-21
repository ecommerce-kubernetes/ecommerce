package com.example.product_service.api.product.saga.service;

import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.product.saga.producer.SagaEventProducer;
import com.example.product_service.api.product.service.VariantService;
import com.example.product_service.api.product.service.dto.command.VariantStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaProcessor {
    private final VariantService variantService;
    private final SagaEventProducer sagaEventProducer;

    public void productSagaProcess(ProductSagaCommand command) {
        try {
            List<VariantStockCommand> stockCommands = mapToStockCommand(command);
            processStockCommand(command.getType(), stockCommands);
            sagaEventProducer.sendDeductionSuccess(command.getSagaId(), command.getOrderNo());
        } catch (BusinessException e) {
            sagaEventProducer.sendDeductionFailure(command.getSagaId(), command.getOrderNo(), e.getErrorCode().name(), e.getErrorCode().getMessage());
        }
    }

    private void processStockCommand(ProductCommandType type, List<VariantStockCommand> commands) {
        switch (type) {
            case DEDUCT_STOCK -> variantService.deductVariantsStock(commands);
            case RESTORE_STOCK -> variantService.restoreVariantsStock(commands);
        }
    }

    private List<VariantStockCommand> mapToStockCommand(ProductSagaCommand command) {
        return command.getItems().stream().map(item -> VariantStockCommand.of(item.getProductVariantId(), item.getQuantity()))
                .toList();
    }
}
