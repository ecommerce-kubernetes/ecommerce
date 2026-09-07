package com.example.product_service.product.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.ProductErrorCode;
import com.example.product_service.product.application.service.dto.command.VariantStockCommand;
import com.example.product_service.product.application.service.dto.result.InternalVariantResponse;
import com.example.product_service.product.domain.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VariantService {

    @Transactional(readOnly = true)
    public InternalVariantResponse getVariant(Long variantId){
        return null;
    }

    @Transactional(readOnly = true)
    public List<InternalVariantResponse> getVariants(List<Long> variantIds) {
        return null;
    }

    public void deductVariantsStock(List<VariantStockCommand> commands) {
    }

    public void restoreVariantsStock(List<VariantStockCommand> commands) {

    }



    private Map<Long, Integer> mapToCommands(List<VariantStockCommand> commands) {
        return commands.stream()
                .collect(Collectors.toMap(VariantStockCommand::getVariantId, VariantStockCommand::getQuantity));
    }
}
