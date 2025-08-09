package com.example.product_service.service;

import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.repository.OptionValuesRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService{
    private final ProductsRepository productsRepository;
    private final OptionValuesRepository optionValuesRepository;
    private final ProductVariantsRepository productVariantsRepository;

    @Override
    public ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request) {
        return null;
    }
}
