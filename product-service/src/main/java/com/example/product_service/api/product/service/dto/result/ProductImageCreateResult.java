package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductImageCreateResult {
    private Long productId;
    private List<ProductImageResult> images;

    @Builder
    private ProductImageCreateResult(List<ProductImageResult> images, Long productId) {
        this.images = images;
        this.productId = productId;
    }

    public static ProductImageCreateResult of(Long productId, List<ProductImage> images) {
        List<ProductImageResult> imageResponses = images.stream().map(ProductImageResult::from).toList();
        return ProductImageCreateResult.builder()
                .productId(productId)
                .images(imageResponses)
                .build();
    }
}
