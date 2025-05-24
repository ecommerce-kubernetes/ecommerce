package com.example.product_service.repository;

import com.example.product_service.entity.OptionTypes;
import com.example.product_service.repository.query.OptionTypesQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionTypesRepository extends JpaRepository<OptionTypes, Long>, OptionTypesQueryRepository {
    Page<OptionTypes> findAll(Pageable pageable);

    List<OptionTypes> findByIdIn(List<Long> ids);
}
