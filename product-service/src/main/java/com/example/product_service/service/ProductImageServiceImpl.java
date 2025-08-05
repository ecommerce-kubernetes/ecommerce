package com.example.product_service.service;

import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService{

    private final ProductImagesRepository productImagesRepository;
    private final ProductsRepository productsRepository;
    private final ImageClientService imageClientService;

}
