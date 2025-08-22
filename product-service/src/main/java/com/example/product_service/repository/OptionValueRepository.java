package com.example.product_service.repository;

import com.example.product_service.entity.OptionValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValues, Long> {
    List<OptionValues> findByIdIn(List<Long> ids);
}
