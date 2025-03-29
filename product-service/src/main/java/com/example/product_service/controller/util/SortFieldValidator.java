package com.example.product_service.controller.util;

import com.example.product_service.exception.BadRequestException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Component
public class SortFieldValidator {
    public void validateSortFields(Sort sort, Class<?> entityClass){
        List<String> validFields = Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .toList();
        for (Sort.Order order : sort){
            if(!validFields.contains(order.getProperty())){
                throw new BadRequestException(order.getProperty() + " is not Entity Field");
            }
        }
    }
}
