package com.example.product_service.service;

import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;

import java.util.List;

public interface ProductImageService {
    List<ImageResponse> addImages(Long productId, AddImageRequest request);
}
