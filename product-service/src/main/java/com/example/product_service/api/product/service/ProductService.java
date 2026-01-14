package com.example.product_service.api.product.service;

import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.service.dto.command.AddVariantCommand;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.dto.response.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    public ProductCreateResponse createProduct(ProductCreateCommand command) {
        return null;
    }

    public ProductOptionSpecResponse addOptionSpec(Long productId, List<Long> optionTypeIds) {
        return null;
    }

    public VariantCreateResponse addVariants(AddVariantCommand command) {
        return null;
    }

    public ProductImageCreateResponse addImages(Long productId, List<String> images) {
        return null;
    }

    public ProductStatusResponse publish(Long productId) {
        return null;
    }

    public PageDto<ProductSummaryResponse> getProducts(ProductSearchCondition condition){
        return null;
    }

    public ProductDetailResponse getProduct(Long productId){
        return null;
    }

    public ProductUpdateResponse updateProduct(ProductUpdateCommand command) {
        return null;
    }

    public void deleteProduct(Long productId) {

    }

    public ProductStatusResponse closedProduct(Long productId) {
        return null;
    }

}
