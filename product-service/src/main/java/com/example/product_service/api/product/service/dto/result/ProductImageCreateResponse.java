package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductImageCreateResponse {
    private Long productId;
    private List<ProductImageResponse> images;

    @Builder
    private ProductImageCreateResponse(List<ProductImageResponse> images, Long productId) {
        this.images = images;
        this.productId = productId;
    }

    public static ProductImageCreateResponse of(Long productId, List<ProductImage> images) {
        List<ProductImageResponse> imageResponses = images.stream().map(ProductImageResponse::from).toList();
        return ProductImageCreateResponse.builder()
                .productId(productId)
                .images(imageResponses)
                .build();
    }
}
