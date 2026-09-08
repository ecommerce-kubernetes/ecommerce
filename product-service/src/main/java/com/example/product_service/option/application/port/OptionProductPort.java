package com.example.product_service.option.application.port;

public interface OptionProductPort {
    boolean existsProductForOptionType(Long optionTypeId);

    boolean existsProductForOptionValue(Long optionValueId);
}
