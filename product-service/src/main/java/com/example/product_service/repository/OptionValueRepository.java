package com.example.product_service.repository;

import com.example.product_service.api.option.domain.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    List<OptionValue> findByIdIn(List<Long> ids);
}
