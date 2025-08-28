package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.ProductVariant;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.InventoryReductionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.product_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantService{
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ReviewResponse addReview(Long variantId, Long userId, ReviewRequest request) {
        return null;
    }

    public ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request) {
        ProductVariant productVariant = findWithProductById(variantId);
        if(request.getPrice() != null){
            productVariant.setPrice(request.getPrice());
        }

        if(request.getStockQuantity() != null){
            productVariant.setStockQuantity(request.getStockQuantity());
        }

        if(request.getDiscountRate() != null){
            productVariant.setDiscountValue(request.getDiscountRate());
        }

        return new ProductVariantResponse(productVariant);
    }

    public void deleteVariantById(Long variantId) {
        ProductVariant productVariant = findWithProductById(variantId);
        productVariant.getProduct().deleteVariant(productVariant);
    }

    public List<InventoryReductionItem> inventoryReductionById(Map<Long, Integer> reductionMap){
        Set<Long> productVariantIds = reductionMap.keySet();
        List<InventoryReductionItem> itemList = new ArrayList<>();
        List<ProductVariant> productVariants = findByIdIn(productVariantIds);
        for (ProductVariant productVariant : productVariants) {
            Integer reductionStock = reductionMap.get(productVariant.getId());
            productVariant.reductionStock(reductionStock);
            itemList.add(new InventoryReductionItem(productVariant, reductionStock));
        }
        return itemList;
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
