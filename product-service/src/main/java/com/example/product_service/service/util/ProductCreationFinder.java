package com.example.product_service.service.util;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
public class ProductCreationFinder {
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final MessageSourceUtil ms;

    public ProductCreationData validAndFind(ProductRequest request){
        Categories categories = findByIdOrThrow(request.getCategoryId());

    }

    private Categories findByIdOrThrow(Long categoryId){
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }
}
