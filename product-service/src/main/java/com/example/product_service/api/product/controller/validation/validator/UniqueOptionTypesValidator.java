package com.example.product_service.api.product.controller.validation.validator;

import com.example.product_service.api.product.controller.dto.request.ProductRequest.ProductOptionRequest;
import com.example.product_service.api.product.controller.validation.annotation.UniqueOptionTypes;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class UniqueOptionTypesValidator implements ConstraintValidator<UniqueOptionTypes, List<ProductOptionRequest>> {

    @Override
    public boolean isValid(List<ProductOptionRequest> options, ConstraintValidatorContext context) {
        if (options == null || options.isEmpty()) {
            return true;
        }

        List<Long> validIds = options.stream()
                .filter(option -> option != null && option.optionTypeId() != null)
                .map(ProductOptionRequest::optionTypeId)
                .toList();

        long uniqueCount = validIds.stream()
                .distinct()
                .count();

        return uniqueCount == validIds.size();
    }
}
