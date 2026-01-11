package com.example.product_service.service;

import com.example.product_service.api.category.domain.repository.CategoryRepository;
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
    private final ProductRepository productRepository;
    private final MessageSourceUtil ms;

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return null;
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
        return null;
    }

    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable){
        boolean isExists = productRepository.existsById(productId);
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

    private Product findWithCategoryByIdOrThrow(Long productId){
        return productRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_NOT_FOUND)));
    }
}
