package com.example.product_service.service.util.validator;

import com.example.product_service.entity.DomainType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductPageableValidator implements PageableValidator{

    private static final Map<String, String> ALLOWED_SORT_MAP = Map.of(
            "price", "minimumPrice",
            "createdAt", "createdAt",
            "reviewCount", "reviewCount",
            "rating", "avgRating"
    );

    private static final Sort DEFAULT_SORT = Sort.by("id").descending();

    @Override
    public boolean support(DomainType domainType) {
        return domainType == DomainType.PRODUCT;
    }

    @Override
    public Pageable validate(Pageable pageable) {
        List<Sort.Order> validatedOrders = new ArrayList<>();
        for(Sort.Order order : pageable.getSort()){
            String clientProp = order.getProperty();
            String mapped = ALLOWED_SORT_MAP.get(clientProp);

            if (mapped == null) {
                continue;
            }

            Sort.Order mappedOrder = new Sort.Order(order.getDirection(), mapped);
            validatedOrders.add(mappedOrder);
        }
        Sort filteredSort = validatedOrders.isEmpty() ? DEFAULT_SORT : Sort.by(validatedOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), filteredSort);
    }
}
