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
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialValidator;
import com.example.product_service.service.util.validator.ProductRequestStructureValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.product_service.common.MessagePath.CATEGORY_NOT_FOUND;
import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSaver {
    private final ProductsRepository productsRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRequestStructureValidator structureValidator;
    private final ProductReferentialValidator referentialValidator;
    private final ProductFactory factory;
    private final MessageSourceUtil ms;

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
        Products target = findProductByIdOrThrow(productId);

        if(request.getName() != null && !request.getName().isEmpty()){
            target.setName(request.getName());
        }

        if(request.getDescription() != null && !request.getDescription().isEmpty()){
            target.setDescription(request.getDescription());
        }

        if(request.getCategoryId() != null){
            Categories category = findCategoryByIdOrThrow(request.getCategoryId());
            target.setCategory(category);
        }

        return new ProductUpdateResponse(target);
    }

    public void deleteProductById(Long productId){
        Products product = findProductByIdOrThrow(productId);
        productsRepository.delete(product);
    }

    public List<ImageResponse> addImages(Long productId, AddImageRequest request){
        Products product = findProductByIdOrThrow(productId);
        List<ProductImages> addImages = new ArrayList<>();
        int size = product.getImages().size();
        for (String imageUrl : request.getImageUrls()) {
            ProductImages productImages = new ProductImages(imageUrl, size++);
            addImages.add(productImages);
        }

        product.addImages(addImages);

        return product.getImages().stream().map(ImageResponse::new).toList();
    }

    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request){
        Products product = findProductByIdOrThrow(productId);
        structureValidator.validateVariantRequest(request, product);
        ProductVariantCreationData productVariantCreationData =
                referentialValidator.validateProductVariant(request, product);

        ProductVariants productVariant = factory.createProductVariant(request, productVariantCreationData);
        product.addVariant(productVariant);
        return new ProductVariantResponse(productVariant);
    }

    private Products findProductByIdOrThrow(Long productId){
        return productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }

    private Categories findCategoryByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

}
