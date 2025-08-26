package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductVariantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.product_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductVariantService{
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ReviewResponse addReview(Long variantId, Long userId, ReviewRequest request) {
        return null;
    }

    public void deleteVariantById(Long variantId) {
        ProductVariants productVariant = findWithProductById(variantId);
        productVariant.getProduct().deleteVariant(productVariant);
    }

    public ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request) {
        return null;
    }

    private ProductVariants findWithProductById(Long variantId){
        return productVariantsRepository.findWithProductById(variantId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND)));
    }
}
