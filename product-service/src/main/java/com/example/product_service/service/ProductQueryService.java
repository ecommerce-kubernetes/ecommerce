package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.dto.ReviewStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private static final int MINIMUM_REVIEW = 5;

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


        return parseProductSummaryResponse(result, pageable);
    }

    public ProductResponse getProductById(Long productId) {
        Product product = findWithCategoryByIdOrThrow(productId);
        List<ProductImage> productImages = productImagesRepository.findByProductId(productId);
        List<ProductOptionType> productOptionTypes = productOptionTypesRepository.findWithOptionTypeByProductId(productId);
        List<ProductVariant> productVariants = productVariantsRepository.findWithVariantOptionByProductId(productId);
        ReviewStats reviewStats = reviewsRepository.findReviewStatsByProductId(productId);
        return new ProductResponse(product, productImages, productOptionTypes, productVariants, reviewStats);
    }

    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId){
        Double C = productSummaryRepository.findAvgRating(); // 상품 전체 평균 평점
        List<Long> categoryIds = getDescendantIds(categoryId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummary> result =
                productSummaryRepository.findPopularProductSummary(categoryIds, C, MINIMUM_REVIEW, pageable);

        return parseProductSummaryResponse(result, pageable);
    }

    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable){
        boolean isExists = productsRepository.existsById(productId);
        if(!isExists){
            throw new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND));
        }

        Page<Review> result = reviewsRepository.findAllByProductId(productId, pageable);
        return parseReviewResponse(result, pageable);
    }

    private PageDto<ProductSummaryResponse> parseProductSummaryResponse(Page<ProductSummary> result, Pageable pageable){
        List<ProductSummaryResponse> content = result.getContent().stream().map(ProductSummaryResponse::new).toList();

        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                result.getTotalPages(),
                pageable.getPageSize(),
                result.getTotalElements()
        );
    }

    private PageDto<ReviewResponse> parseReviewResponse(Page<Review> result, Pageable pageable){
        List<ReviewResponse> content = result.getContent().stream().map(ReviewResponse::new).toList();

        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                result.getTotalPages(),
                pageable.getPageSize(),
                result.getTotalElements()
        );
    }

    private List<Long> getDescendantIds(Long categoryId){
        if(categoryId == null){
            return List.of();
        }
        return categoryRepository.findDescendantIds(categoryId);
    }

    private Product findWithCategoryByIdOrThrow(Long productId){
        return productsRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }
}
