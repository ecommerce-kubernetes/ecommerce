package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.ItemOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemOptionDto {
    private String optionTypeName;
    private String optionValueName;

    public static ItemOptionDto from(ItemOption itemOption) {
        return ItemOptionDto.builder()
                .optionTypeName(itemOption.getOptionTypeName())
                .optionValueName(itemOption.getOptionValueName())
                .build();
    }
}
