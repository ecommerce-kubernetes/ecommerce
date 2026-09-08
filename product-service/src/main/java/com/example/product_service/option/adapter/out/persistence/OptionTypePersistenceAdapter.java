package com.example.product_service.option.adapter.out.persistence;

import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.domain.OptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OptionTypePersistenceAdapter implements OptionTypeRepository {

    private final OptionTypeJpaRepository optionTypeJpaRepository;

    @Override
    public boolean existsByName(String name) {
        return optionTypeJpaRepository.existsByName(name);
    }

    @Override
    public OptionType save(OptionType optionType) {
        return optionTypeJpaRepository.save(optionType);
    }
}
