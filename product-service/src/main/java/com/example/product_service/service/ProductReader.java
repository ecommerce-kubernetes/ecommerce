package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.entity.DomainType;
import com.example.product_service.entity.ProductSummary;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductSummaryRepository;
import com.example.product_service.service.util.validator.PageableValidator;
import com.example.product_service.service.util.validator.PageableValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReader {
    private final CategoryRepository categoryRepository;
    private final ProductSummaryRepository productSummaryRepository;
    private final PageableValidatorFactory pageableValidatorFactory;

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        PageableValidator validator = pageableValidatorFactory.getValidator(DomainType.PRODUCT);
        Pageable verifiedPageable = validator.validate(pageable);
        List<Long> descendantIds = getDescendantIds(search.getCategoryId());
        Page<ProductSummary> result = productSummaryRepository
                .findAllProductSummary(search.getName(), descendantIds, search.getRating(), verifiedPageable);

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

    private List<Long> getDescendantIds(Long categoryId){
        if(categoryId == null){
            return List.of();
        }
        return categoryRepository.findDescendantIds(categoryId);
    }
}
