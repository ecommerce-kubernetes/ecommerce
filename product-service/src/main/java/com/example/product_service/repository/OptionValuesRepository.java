package com.example.product_service.repository;

import com.example.product_service.entity.OptionValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionValuesRepository extends JpaRepository<OptionValues, Long> {
    List<OptionValues> findByIdIn(List<Long> ids);
}
