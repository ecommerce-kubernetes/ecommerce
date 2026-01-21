package com.example.product_service.api.product.service;

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
}
