package com.example.order_service.controller.util.validator;

import com.example.order_service.entity.DomainType;
import org.springframework.data.domain.Pageable;

public interface PageableValidator {
    boolean support(DomainType domainType);
    Pageable validate(Pageable pageable);
}
