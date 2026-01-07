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
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductImage;
import com.example.product_service.entity.ProductVariant;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.dto.*;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferenceService;
import com.example.product_service.service.util.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final RequestValidator requestValidator;
    private final ProductReferenceService productReferenceService;
    private final ProductFactory factory;
    private final MessageSourceUtil ms;

    public ProductResponse saveProduct(ProductRequest request){
        //요청 바디 유효성 검사
        ProductCreationCommand productCreationCommand = requestValidator.validateProductRequest(request);
        //상품 sku 중복, 옵션 타입 연관관계 체크
        ProductCreationData creationData = productReferenceService.buildCreationData(request);
        Product product = factory.createProducts(productCreationCommand, creationData);

        Product saved = productRepository.save(product);
        return new ProductResponse(saved);
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request){
        Product target = findProductByIdOrThrow(productId);

        if(request.getName() != null && !request.getName().isEmpty()){
            target.setName(request.getName());
        }

        if(request.getDescription() != null && !request.getDescription().isEmpty()){
            target.setDescription(request.getDescription());
        }

        if(request.getCategoryId() != null){
            ProductUpdateData productUpdateData = productReferenceService.resolveUpdateData(request);
            target.setCategory(productUpdateData.getCategory());
        }

        return new ProductUpdateResponse(target);
    }

    public void deleteProductById(Long productId){
        Product target = findProductByIdOrThrow(productId);
        productRepository.delete(target);
    }

    public List<ImageResponse> addImages(Long productId, AddImageRequest request){
        Product product = findProductByIdOrThrow(productId);
        List<ProductImage> productImages = request.getImageUrls().stream().map(ProductImage::new).toList();
        product.addImages(productImages);

        return product.getImages().stream().map(ImageResponse::new).toList();
    }

    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request){
        Product product = findProductByIdOrThrow(productId);
        ProductVariantCommand productVariantCommand = requestValidator.validateVariantRequest(request);
        ProductVariantCreationData creationData = productReferenceService.buildVariantCreationData(request);
        ProductVariant productVariant = factory.createProductVariant(productVariantCommand, creationData);
        product.addVariant(productVariant);
        return new ProductVariantResponse(productVariant);
    }

    private Product findProductByIdOrThrow(Long productId){
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }
}
