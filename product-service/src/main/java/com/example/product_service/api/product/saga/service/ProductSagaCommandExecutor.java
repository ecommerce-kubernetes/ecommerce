package com.example.product_service.api.product.saga.service;

import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.product.saga.domain.model.ProcessedSagaEvent;
import com.example.product_service.api.product.saga.domain.repository.ProcessedSagaEventRepository;
import com.example.product_service.api.product.service.VariantService;
import com.example.product_service.api.product.service.dto.command.VariantStockCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSagaCommandExecutor {
    private final VariantService variantService;
    private final ProcessedSagaEventRepository eventRepository;

    public boolean processSagaCommand(ProductSagaCommand command) {
        if (eventRepository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name())){
            return true;
        }
        List<VariantStockCommand> stockCommands = mapToStockCommand(command);
        if (command.getType() == ProductCommandType.DEDUCT_STOCK) {
            variantService.deductVariantsStock(stockCommands);
        } else {
            variantService.restoreVariantsStock(stockCommands);
        }

        ProcessedSagaEvent event = ProcessedSagaEvent.create(command.getSagaId(), command.getType().name());
        eventRepository.save(event);
        return false;
    }

    private List<VariantStockCommand> mapToStockCommand(ProductSagaCommand command) {
        return command.getItems().stream().map(item -> VariantStockCommand.of(item.getProductVariantId(), item.getQuantity()))
                .toList();
    }

}
