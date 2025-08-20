package com.example.product_service.service;

import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.entity.Products;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialValidator;
import com.example.product_service.service.util.validator.ProductRequestStructureValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSaver {
    private final ProductsRepository productsRepository;
    private final ProductRequestStructureValidator structureValidator;
    private final ProductReferentialValidator referentialValidator;
    private final ProductFactory factory;

    public ProductResponse saveProduct(ProductRequest request) {
        //요청 바디 유효성 검사
        structureValidator.validateProductRequest(request);
        //상품 sku 중복, 옵션 타입 연관관계 체크
        ProductCreationData creationData = referentialValidator.validAndFetch(request);
        Products products = factory.createProducts(request, creationData);

        Products saved = productsRepository.save(products);
        return new ProductResponse(saved);
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request){
        return new ProductUpdateResponse();
    }

}
