package com.example.product_service.controller.util;

import com.example.product_service.exception.BadRequestException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SortFieldValidator {
    public void validateSortFields(Sort sort, Class<?> entityClass){

        List<String> validFields = new ArrayList<>();
        Class<?> current = entityClass;

        while(current != null && current != Object.class){
            for(Field field : current.getDeclaredFields()){
                validFields.add(field.getName());
            }
            current = current.getSuperclass();
        }

        for(Sort.Order order : sort) {
            if(!validFields.contains(order.getProperty())){
                throw new BadRequestException(order.getProperty() + " is not Entity Field");
            }
        }
    }
}
