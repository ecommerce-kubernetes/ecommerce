package com.example.product_service.option.application.port;

import com.example.product_service.option.domain.OptionType;

import java.util.List;
import java.util.Optional;

public interface OptionTypeRepository {
    OptionType save(OptionType optionType);

    Optional<OptionType> findById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    void delete(OptionType optionType);

    List<OptionType> findAll();
}
