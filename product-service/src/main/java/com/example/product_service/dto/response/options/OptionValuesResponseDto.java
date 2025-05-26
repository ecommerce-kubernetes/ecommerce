package com.example.product_service.dto.response.options;

import com.example.product_service.entity.OptionValues;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionValuesResponseDto {
    private Long id;
    private String optionValue;
    private Long optionTypeId;

    public OptionValuesResponseDto(OptionValues optionValue) {
        this.id = optionValue.getId();
        this.optionValue = optionValue.getOptionValue();
        this.optionTypeId = optionValue.getOptionType().getId();
    }
}
