package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductImageResult {
    //TODO 이미지 Id 매핑
    private Long imageId;
    private String imageUrl;
    private Integer sortOrder;
    private boolean isThumbnail;

    @Builder
    private ProductImageResult(String imageUrl, Integer sortOrder, boolean isThumbnail) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.isThumbnail = isThumbnail;
    }

    public static ProductImageResult from(ProductImage productImage) {
        return ProductImageResult.builder()
                .imageUrl(productImage.getImageUrl())
                .sortOrder(productImage.getSortOrder())
                .isThumbnail(productImage.isThumbnail())
                .build();
    }
}
