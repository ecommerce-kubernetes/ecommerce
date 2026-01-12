package com.example.product_service.api.product.service.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ImageCreateResponse {
    private Long productId;
    private List<ProductImageResponse> images;

    public static class ProductImageResponse {
        private Long productImageId;
        private String imageUrl;
        private Integer order;
        private boolean isThumbnail;
    }
}
