package com.example.product_service.dto.response.options;

import com.example.product_service.entity.OptionTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OptionTypesDetailResponseDto {
    private OptionTypesResponseDto optionType;
    private List<OptionValuesResponseDto> optionValues;

    public OptionTypesDetailResponseDto(OptionTypes optionTypes){
        this.optionType = new OptionTypesResponseDto(optionTypes);
        this.optionValues = optionTypes.getOptionValues().stream()
                .map(OptionValuesResponseDto::new).toList();
    }
}
