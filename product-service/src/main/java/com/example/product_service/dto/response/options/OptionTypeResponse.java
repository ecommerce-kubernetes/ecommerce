package com.example.product_service.dto.response.options;

import com.example.product_service.api.option.domain.model.OptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionTypeResponse {
    private Long id;
    private String name;

    public OptionTypeResponse(OptionType optionType){
        this.id = optionType.getId();
        this.name = optionType.getName();
    }
}
