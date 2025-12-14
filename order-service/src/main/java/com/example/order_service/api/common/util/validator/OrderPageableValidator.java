package com.example.order_service.api.common.util.validator;

import com.example.order_service.api.common.util.DomainType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPageableValidator implements PageableValidator{
    private static final List<String> ALLOWED_SORT_LIST = List.of("createAt", "id");
    private static final Sort DEFAULT_SORT = Sort.by("createdAt").descending();

    @Override
    public boolean support(DomainType domainType) {
        return domainType == DomainType.ORDER;
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
