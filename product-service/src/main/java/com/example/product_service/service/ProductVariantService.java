package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.OrderProductVariantResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductVariantsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.product_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantService{
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public Map<Long, Integer> inventoryReductionById(Map<Long, Integer> reductionMap){
        Set<Long> productVariantIds = reductionMap.keySet();
        List<ProductVariant> productVariants = findByIdIn(productVariantIds);
        Map<Long, Integer> resultMap = new HashMap<>();
        for (ProductVariant productVariant : productVariants) {
            Integer reductionStock = reductionMap.get(productVariant.getId());
            productVariant.reductionStock(reductionStock);
            resultMap.put(productVariant.getId(), reductionStock);
        }
        return resultMap;
    }

    public List<OrderProductVariantResponse> getOrderVariantByIds(List<Long> variantIds){
        List<ProductVariant> variants = productVariantsRepository.findWithProductAndOptionsByIds(variantIds);
        return variants.stream().map(OrderProductVariantResponse::new).toList();
    }

    public void inventoryRestorationById(Map<Long, Integer> restoreMap){
        Set<Long> productVariantIds = restoreMap.keySet();
        List<ProductVariant> productVariants = findByIdIn(productVariantIds);
        for (ProductVariant productVariant : productVariants) {
            Integer restorationStock = restoreMap.get(productVariant.getId());
            productVariant.restoreStock(restorationStock);
        }
    }

    private ProductVariant findWithProductById(Long variantId){
        return productVariantsRepository.findWithProductById(variantId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND)));
    }

    private List<ProductVariant> findByIdIn(Set<Long> ids){
        List<ProductVariant> result = productVariantsRepository.findByIdIn(ids);
        if(ids.size() != result.size()){
            throw new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND));
        }

        return result;
    }
}
