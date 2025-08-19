package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.dto.ReviewStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReader {
    private final CategoryRepository categoryRepository;
    private final ProductSummaryRepository productSummaryRepository;
    private final ProductOptionTypesRepository productOptionTypesRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final ProductImagesRepository productImagesRepository;
    private final ReviewsRepository reviewsRepository;
    private final ProductsRepository productsRepository;
    private final MessageSourceUtil ms;

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        List<Long> descendantIds = getDescendantIds(search.getCategoryId());
        Page<ProductSummary> result = productSummaryRepository
                .findAllProductSummary(search.getName(), descendantIds, search.getRating(), pageable);

        List<ProductSummaryResponse> content =
                result.getContent().stream().map(ProductSummaryResponse::new).toList();

        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                result.getTotalPages(),
                pageable.getPageSize(),
                result.getTotalElements()
        );
    }

    public ProductResponse getProductById(Long productId) {
        Products product = findWithCategoryByIdOrThrow(productId);
        List<ProductImages> productImages = productImagesRepository.findByProductId(productId);
        List<ProductOptionTypes> productOptionTypes = productOptionTypesRepository.findWithOptionTypeByProductId(productId);
        List<ProductVariants> productVariants = productVariantsRepository.findWithVariantOptionByProductId(productId);
        ReviewStats reviewStats = reviewsRepository.findReviewStatsByProductId(productId);
        return new ProductResponse(product, productImages, productOptionTypes, productVariants, reviewStats);
    }

    private List<Long> getDescendantIds(Long categoryId){
        if(categoryId == null){
            return List.of();
        }
        return categoryRepository.findDescendantIds(categoryId);
    }

    private Products findWithCategoryByIdOrThrow(Long productId){
        return productsRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }
}
