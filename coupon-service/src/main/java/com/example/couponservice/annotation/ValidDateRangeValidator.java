package com.example.couponservice.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, HasValidDateRange> {

    @Override
    public boolean isValid(HasValidDateRange dto, ConstraintValidatorContext context) {
        if (dto.getValidFrom() == null || dto.getValidTo() == null) {
            return true;
        }

        return dto.getValidTo().isAfter(dto.getValidFrom());
    }
}
