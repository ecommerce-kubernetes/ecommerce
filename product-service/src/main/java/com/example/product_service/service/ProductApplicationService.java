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
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductUpdateData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialService;
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
    private final ProductSaver productSaver;

    private final ProductsRepository productsRepository;
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
        Products target = findProductByIdOrThrow(productId);

        if(request.getName() != null && !request.getName().isEmpty()){
            target.setName(request.getName());
        }

        if(request.getDescription() != null && !request.getDescription().isEmpty()){
            target.setDescription(request.getDescription());
        }

        if(request.getCategoryId() != null){
            ProductUpdateData productUpdateData = productReferentialService.validateUpdateProduct(request);
            target.setCategory(productUpdateData.getCategories());
        }

        return new ProductUpdateResponse(target);
    }

    public void deleteProductById(Long productId){
        Products target = findProductByIdOrThrow(productId);
        productsRepository.delete(target);
    }

    public List<ImageResponse> addImages(Long productId, AddImageRequest request){
        Products product = findProductByIdOrThrow(productId);
        List<ProductImages> productImages = request.getImageUrls().stream().map(ProductImages::new).toList();
        product.addImages(productImages);

        return product.getImages().stream().map(ImageResponse::new).toList();
    }

    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request){
        Products product = findProductByIdOrThrow(productId);
        requestValidator.validateVariantRequest(request);
        ProductVariantCreationData creationData = productReferentialService.validateProductVariant(request);
        ProductVariants productVariant = factory.createProductVariant(request, creationData);
        product.addVariant(productVariant);
        return new ProductVariantResponse(productVariant);
    }

    private Products findProductByIdOrThrow(Long productId){
        return productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }
}
