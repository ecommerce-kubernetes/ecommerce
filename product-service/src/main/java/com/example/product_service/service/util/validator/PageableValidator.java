package com.example.product_service.service.util.validator;

import com.example.product_service.entity.DomainType;
import org.springframework.data.domain.Pageable;

public interface PageableValidator {
    boolean support(DomainType domainType);
    Pageable validate(Pageable pageable);
}
