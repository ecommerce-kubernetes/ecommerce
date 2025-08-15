package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.repository.*;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.ProductReferentialValidator;
import com.example.product_service.service.util.ProductRequestStructureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductsRepository productsRepository;
    private final ProductRequestStructureValidator structureValidator;
    private final ProductReferentialValidator referentialValidator;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        //요청 바디 유효성 검사
        structureValidator.validateProductRequest(request);
        //상품 sku 중복, 옵션 타입 연관관계 체크
        ProductCreationData creationData = referentialValidator.validAndFetch(request);

        Products products = new Products(request.getName(), request.getDescription(), creationData.getCategory());
        for(ImageRequest imageRequest : request.getImages()){
            ProductImages productImage = new ProductImages(imageRequest.getUrl(), imageRequest.getSortOrder());
            products.addImage(productImage);
        }

        for(ProductOptionTypeRequest productOptionTypeRequest : request.getProductOptionTypes()){
            OptionTypes optionType = creationData.getOptionTypeById().get(productOptionTypeRequest.getOptionTypeId());
            products.addOptionType(new ProductOptionTypes(optionType, productOptionTypeRequest.getPriority(), true));
        }

        for(ProductVariantRequest variantRequest :request.getProductVariants()){
            ProductVariants productVariants =
                    new ProductVariants(variantRequest.getSku(), variantRequest.getPrice(), variantRequest.getStockQuantity(), variantRequest.getDiscountRate());

            for(VariantOptionValueRequest valueRequest : variantRequest.getVariantOption()){
                OptionValues optionValues = creationData.getOptionValueById().get(valueRequest.getOptionValueId());
                productVariants.addProductVariantOption(new ProductVariantOptions(optionValues));
            }
            products.addVariant(productVariants);
        }

        Products saved = productsRepository.save(products);
        return new ProductResponse(saved);
    }


    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request) {
        return null;
    }

    public void deleteProductById(Long productId) {

    }


    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return null;
    }


    public ProductResponse getProductById(Long productId) {
        return null;
    }


    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return null;
    }


    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return null;
    }



    public List<ImageResponse> addImages(Long productId, AddImageRequest request) {
        return List.of();
    }


    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        return null;
    }

}
