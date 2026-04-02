package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductDescriptionImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductDescriptionImageCreateResponse {
    private Long productId;
    private List<ProductDescriptionImageResponse> descriptionImages;

    public static ProductDescriptionImageCreateResponse of(Long productId, List<ProductDescriptionImage> images) {
        List<ProductDescriptionImageResponse> descriptionImageResponses = images.stream().map(ProductDescriptionImageResponse::from).toList();
        return ProductDescriptionImageCreateResponse.builder()
                .productId(productId)
                .descriptionImages(descriptionImageResponses)
                .build();
    }

}
