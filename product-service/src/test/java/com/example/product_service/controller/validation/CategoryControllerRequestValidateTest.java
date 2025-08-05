package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class CategoryControllerRequestValidateTest {
    private static final String INVALID_URL = "invalidUrl";
    private static final String VALID_URL = "http://test.jpg";

    private Validator validator;

    @BeforeEach
    void setUp(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidCategoryRequestFieldProvider")
    void categoryRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        CategoryRequest request =
                new CategoryRequest("노트북", 1L, VALID_URL);
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(request);
        beanWrapper.setPropertyValue(fieldName, invalidValue);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)
                        && v.getMessage().equals(expectedMessage));
    }

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidUpdateCategoryRequestFiledProvider")
    void updateCategoryRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        UpdateCategoryRequest request = new UpdateCategoryRequest("노트북", 1L, VALID_URL);
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(request);
        beanWrapper.setPropertyValue(fieldName, invalidValue);

        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)
                && v.getMessage().equals(expectedMessage));

    }

    @Test
    @DisplayName("Category Request 필드 동시 오류 발생시 전체 개수 및 필드 확인")
    void categoryRequestValidation_multiple(){
        CategoryRequest request = new CategoryRequest("", 1L, INVALID_URL);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);

        List<String> fields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(fields)
                .containsExactlyInAnyOrder("name", "iconUrl");
    }

    static Stream<Arguments> invalidCategoryRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", "Category name is required"),
                Arguments.of("iconUrl", INVALID_URL, "Invalid ImgUrl")
        );
    }

    static Stream<Arguments> invalidUpdateCategoryRequestFiledProvider(){
        return Stream.of(
                Arguments.of("iconUrl", INVALID_URL, "Invalid ImgUrl")
        );
    }
}
