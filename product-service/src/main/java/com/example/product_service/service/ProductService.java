package com.example.product_service.service;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductsRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.CATEGORY_NOT_FOUND;
import static com.example.product_service.common.MessagePath.OPTION_TYPE_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductsRepository productsRepository;
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Categories category = findCategoryByIdOrThrow(request.getCategoryId());
        Map<OptionTypes, Integer> productOptionTypeMap = getProductOptionTypeMap(request.getProductOptionTypes());

        return new ProductResponse();
    }

    private Map<OptionTypes, Integer> getProductOptionTypeMap(List<ProductOptionTypeRequest> requestOptionTypes){
        return requestOptionTypes.stream()
                .collect(Collectors.toMap(ot -> findOptionTypeByIdOrThrow(ot.getOptionTypeId()),
                        ProductOptionTypeRequest::getPriority));
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


    private Categories findCategoryByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

    private OptionTypes findOptionTypeByIdOrThrow(Long optionTypeId){
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND)));
    }
}
