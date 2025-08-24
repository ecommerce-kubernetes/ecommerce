package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialService;
import com.example.product_service.service.util.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    private final ProductSaver productSaver;

    private final ProductsRepository productsRepository;
    private final CategoryRepository categoryRepository;
    private final RequestValidator requestValidator;
    private final ProductReferentialService productReferentialService;
    private final ProductFactory factory;
    private final MessageSourceUtil ms;

    public ProductResponse saveProduct(ProductRequest request){
        //요청 바디 유효성 검사
        requestValidator.validateProductRequest(request);
        //상품 sku 중복, 옵션 타입 연관관계 체크
        ProductCreationData creationData = productReferentialService.validAndFetch(request);
        Products products = factory.createProducts(request, creationData);

        Products saved = productsRepository.save(products);
        return new ProductResponse(saved);
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
