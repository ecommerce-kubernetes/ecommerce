package com.example.product_service.controller.util.validator;

import com.example.product_service.entity.DomainType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewPageableValidator implements PageableValidator{
    private static final List<String> ALLOWED_SORT_LIST = List.of("createAt", "rating");
    private static final Sort DEFAULT_SORT = Sort.by("createAt").descending();
    @Override
    public boolean support(DomainType domainType) {
        return domainType == DomainType.REVIEW;
    }

    @Override
    public Pageable validate(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (!ALLOWED_SORT_LIST.contains(property)) {
                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }
}
