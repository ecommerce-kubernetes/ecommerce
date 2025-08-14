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

public interface OptionTypeRepository extends JpaRepository<OptionTypes, Long>, OptionTypesQueryRepository {

    boolean existsByName(String name);

    Page<OptionTypes> findAll(Pageable pageable);

    @Query("SELECT DISTINCT ot FROM OptionTypes ot LEFT JOIN FETCH ot.optionValues ov WHERE ot.id IN :ids")
    List<OptionTypes> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT o FROM OptionTypes o LEFT JOIN FETCH o.optionValues WHERE o.id = :optionTypeId")
    Optional<OptionTypes> findByIdWithOptionValues(@Param("optionTypeId") Long optionTypeId);
}
