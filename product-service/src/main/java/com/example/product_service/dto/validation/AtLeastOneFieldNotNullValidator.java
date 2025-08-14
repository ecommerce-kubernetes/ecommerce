package com.example.product_service.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {
    private String[] fieldNames;
    @Override
    public void initialize(AtLeastOneFieldNotNull constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fieldNames();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        if(o == null){
            return false;
        }
        try{
            if(fieldNames != null && fieldNames.length > 0){
                for(String name : fieldNames){
                    Field field = o.getClass().getDeclaredField(name);
                    field.setAccessible(true);
                    Object fieldVal = field.get(o);
                    if(isPresent(fieldVal)) return true;
                }
                return false;
            } else {
                for(Field field : o.getClass().getDeclaredFields()){
                    if(Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    Object fieldVal = field.get(o);
                    if(isPresent(fieldVal)) return true;
                }
            }
            return false;
        } catch (Exception e){
            return false;
        }
    }

    private boolean isPresent(Object fieldVal) {
        if (fieldVal == null) return false;
        if (fieldVal instanceof CharSequence) {
            return !((CharSequence) fieldVal).toString().trim().isEmpty();
        }
        return true;
    }
}
