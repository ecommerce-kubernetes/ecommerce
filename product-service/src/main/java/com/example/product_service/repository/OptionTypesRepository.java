package com.example.product_service.repository;

import com.example.product_service.entity.OptionTypes;
import com.example.product_service.repository.query.OptionTypesQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionTypesRepository extends JpaRepository<OptionTypes, Long>, OptionTypesQueryRepository {
    Page<OptionTypes> findAll(Pageable pageable);
    List<OptionTypes> findByIdIn(List<Long> ids);

    @Query("SELECT o FROM OptionTypes o LEFT JOIN FETCH o.optionValues WHERE o.id = :optionTypeId")
    Optional<OptionTypes> findByIdWithOptionValues(@Param("optionTypeId") Long optionTypeId);
}
