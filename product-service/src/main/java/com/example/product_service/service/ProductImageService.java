package com.example.product_service.service;

import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;

import java.util.List;

public interface ProductImageService {
    List<ImageResponse> addImages(Long productId, AddImageRequest request);
    ImageResponse updateImageById(Long imageId, ImageRequest request);
    void deleteImageById(Long imageId);
}
