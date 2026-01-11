package com.example.product_service.repository;

import com.example.product_service.api.option.domain.OptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionTypeRepository extends JpaRepository<OptionType, Long>{

    boolean existsByName(String name);

    Page<OptionType> findAll(Pageable pageable);

    @Query("SELECT DISTINCT ot FROM OptionType ot LEFT JOIN FETCH ot.optionValues ov WHERE ot.id IN :ids")
    List<OptionType> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT o FROM OptionType o LEFT JOIN FETCH o.optionValues WHERE o.id = :optionTypeId")
    Optional<OptionType> findByIdWithOptionValues(@Param("optionTypeId") Long optionTypeId);
}
