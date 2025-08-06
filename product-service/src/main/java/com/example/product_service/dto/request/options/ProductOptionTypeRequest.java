package com.example.product_service.dto.request.options;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionTypeRequest {
    @NotNull(message = "{productOptionType.optionTypeId.notNull}")
    private Long optionTypeId;
    @NotNull(message = "{productOptionType.priority.notNull}")
    @Min(value = 0, message = "{productOptionType.priority.min}")
    private Integer priority;
}
