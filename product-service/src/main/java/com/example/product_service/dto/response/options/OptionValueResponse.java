package com.example.product_service.dto.response.options;

import com.example.product_service.api.option.domain.OptionValue;
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
    private String valueName;

    public OptionValueResponse(OptionValue optionValue){
        this.valueId = optionValue.getId();
        this.typeId = optionValue.getOptionType().getId();
        this.valueName = optionValue.getName();
    }
}
