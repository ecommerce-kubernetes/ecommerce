package com.example.product_service.service;

import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.repository.OptionValueRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService{
    @Override
    public ReviewResponse addReview(Long variantId, Long userId, ReviewRequest request) {
        return null;
    }

    private final ProductsRepository productsRepository;
    private final OptionValueRepository optionValuesRepository;
    private final ProductVariantsRepository productVariantsRepository;

    @Override
    public void deleteVariantById(Long variantId) {

    }

    @Override
    public ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request) {
        return null;
    }
}
