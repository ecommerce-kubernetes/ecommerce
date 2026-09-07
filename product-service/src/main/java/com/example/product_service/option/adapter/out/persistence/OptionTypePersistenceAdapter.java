package com.example.product_service.option.adapter.out.persistence;

import com.example.product_service.option.application.port.OptionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OptionTypePersistenceAdapter implements OptionTypeRepository {

    private final OptionTypeJpaRepository optionTypeJpaRepository;
}
