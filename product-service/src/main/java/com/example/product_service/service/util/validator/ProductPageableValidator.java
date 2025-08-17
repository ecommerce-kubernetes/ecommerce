package com.example.product_service.service.util.validator;

import com.example.product_service.entity.DomainType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductPageableValidator implements PageableValidator{

    private static final List<String> ALLOWED_SORT_FILED = List.of("price", "createdAt", "reviewCount", "rating");

    @Override
    public boolean support(DomainType domainType) {
        return domainType == DomainType.PRODUCT;
    }

    @Override
    public Pageable validate(Pageable pageable) {
        Sort filteredSort = Sort.by(
                pageable.getSort().stream()
                        .filter(order -> ALLOWED_SORT_FILED.contains(order.getProperty()))
                        .toList());

        if(filteredSort.isUnsorted()){
            filteredSort = Sort.by("id").descending();
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), filteredSort);
    }
}
