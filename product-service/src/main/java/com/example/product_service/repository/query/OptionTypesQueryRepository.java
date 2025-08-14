package com.example.product_service.repository.query;


import com.example.product_service.entity.OptionTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OptionTypesQueryRepository {
    Page<OptionTypes> findByNameOrAll(String query, Pageable pageable);
}
