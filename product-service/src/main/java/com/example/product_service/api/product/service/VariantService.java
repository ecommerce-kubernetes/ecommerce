package com.example.product_service.api.product.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductVariantRepository;
import com.example.product_service.api.product.service.dto.command.VariantStockCommand;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final ProductVariantRepository productVariantRepository;

    public InternalVariantResponse getVariant(Long variantId){
        ProductVariant variant = findVariantOrThrow(variantId);
        return InternalVariantResponse.from(variant);
    }

    public List<InternalVariantResponse> getVariants(List<Long> variantIds) {
        List<ProductVariant> variants = productVariantRepository.findByIdInWithProductAndOption(variantIds);
        return variants.stream().map(InternalVariantResponse::from).toList();
    }

    public void deductVariantsStock(List<VariantStockCommand> commands) {
    }

    public void restoreVariantsStock(List<VariantStockCommand> commands) {
    }

    private ProductVariant findVariantOrThrow(Long variantId){
        return productVariantRepository.findByIdWithProductAndOption(variantId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }
}
