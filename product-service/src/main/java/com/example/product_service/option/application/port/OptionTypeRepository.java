package com.example.product_service.option.application.port;

import com.example.product_service.option.domain.OptionType;

public interface OptionTypeRepository {
    OptionType save(OptionType optionType);
    boolean existsByName(String name);
}
