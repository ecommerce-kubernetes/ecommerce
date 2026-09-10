package com.example.product_service.option.adapter.out.client;

import com.example.product_service.option.application.port.OptionProductPort;
import org.springframework.stereotype.Component;

@Component
public class OptionProductAdapter implements OptionProductPort {
    @Override
    public boolean existsProductForOptionType(Long optionTypeId) {
        return false;
    }

    @Override
    public boolean existsProductForOptionValue(Long optionValueId) {
        return false;
    }
}
