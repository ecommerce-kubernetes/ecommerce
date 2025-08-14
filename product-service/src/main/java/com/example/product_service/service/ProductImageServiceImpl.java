package com.example.product_service.service;

import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService{

    @Override
    public ImageResponse updateImageById(Long imageId, ImageRequest request) {
        return null;
    }

    @Override
    public void deleteImageById(Long imageId) {

    }
}
