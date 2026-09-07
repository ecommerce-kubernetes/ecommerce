package com.example.product_service.option.adapter.out.persistence;

import com.example.product_service.option.domain.OptionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionTypeJpaRepository extends JpaRepository<OptionType, Long> {
}
