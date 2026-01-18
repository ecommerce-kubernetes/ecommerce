package com.example.product_service.api.product.service;

import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductOption;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SkuGenerator {
    public String generate(Product product, List<OptionValue> optionValues) {
        String prefix = "PROD" + product.getId();

        Map<Long, Integer> priorityMap = product.getOptions().stream()
                .collect(Collectors.toMap(op -> op.getOptionType().getId(), ProductOption::getPriority));

        String optionCode = optionValues.stream()
                .sorted(Comparator.comparingInt(val -> priorityMap.getOrDefault(val.getOptionType().getId(), 999)))
                .map(OptionValue::getName)
                .collect(Collectors.joining("-"));

        return String.format("%s-%s", prefix, optionCode);
    }
}
