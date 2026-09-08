package com.example.product_service.option.adapter.out.persistence;

import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.domain.OptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<OptionType> findById(Long id) {
        return optionTypeJpaRepository.findById(id);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return optionTypeJpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public void delete(OptionType optionType) {
        optionTypeJpaRepository.delete(optionType);
    }

    @Override
    public List<OptionType> findAll() {
        return optionTypeJpaRepository.findAll();
    }
}
