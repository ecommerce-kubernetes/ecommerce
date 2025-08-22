package com.example.product_service.service;

import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    private final ProductSaver productSaver;

    public ProductResponse saveProduct(ProductRequest request){
        return productSaver.saveProduct(request);
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request){
        return productSaver.updateBasicInfoById(productId, request);
    }

    public void deleteProductById(Long productId){
        productSaver.deleteProductById(productId);
    }

    public List<ImageResponse> addImages(Long productId, AddImageRequest request){
        return productSaver.addImages(productId, request);
    }

    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request){
        return productSaver.addVariant(productId, request);
    }
}
