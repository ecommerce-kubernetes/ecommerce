package com.example.product_service.service;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductImageService {


    public ImageResponse updateImageById(Long imageId, ImageRequest request) {
        return null;
    }

    public void deleteImageById(Long imageId) {

    }
}
