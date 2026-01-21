package com.example.product_service.api.product.service;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductVariantRepository;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final ProductVariantRepository productVariantRepository;

    public InternalVariantResponse getVariant(Long variantId){
        return null;
    }

    public List<InternalVariantResponse> getVariants(List<Long> variantIds) {
        return null;
    }

    private ProductVariant findVariantOrThrow(Long variantId){
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }
}
