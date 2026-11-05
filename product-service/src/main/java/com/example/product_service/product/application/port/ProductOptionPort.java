package com.example.product_service.product.application.port;

import com.example.product_service.product.application.port.dto.ProductOptionTypesResult;
import com.example.product_service.product.application.port.dto.ProductOptionValuesResult;

import java.util.List;

public interface ProductOptionPort {

    ProductOptionTypesResult getOptionTypes(List<Long> optionTypeIds);

    ProductOptionValuesResult getOptionValues(List<Long> optionValueIds);
}
