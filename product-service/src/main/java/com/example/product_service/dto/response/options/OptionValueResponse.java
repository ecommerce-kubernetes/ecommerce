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
public class OptionValueResponse {
    private Long valueId;
    private Long typeId;
    private String value;

    public OptionValueResponse(OptionValues optionValue){
        this.valueId = optionValue.getId();
        this.typeId = optionValue.getOptionType().getId();
        this.value = optionValue.getValue();
    }
}
