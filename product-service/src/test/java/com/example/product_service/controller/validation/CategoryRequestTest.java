package com.example.product_service.controller.validation;

import com.example.product_service.dto.request.category.CategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class CategoryRequestTest {
    private static final String INVALID_URL = "invalidUrl";
    private static final String VALID_URL = "http://test.jpg";

    private Validator validator;

    @BeforeEach
    void setUp(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidFieldProvider")
    void validation_field(String fieldName, Object invalidValue){
        CategoryRequest request =
                new CategoryRequest("노트북", 1L, VALID_URL);
        BeanWrapperImpl categoryRequest = new BeanWrapperImpl(request);
        categoryRequest.setPropertyValue(fieldName, invalidValue);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
    }

    static Stream<Arguments> invalidFieldProvider(){
        return Stream.of(
                Arguments.of("name", ""),
                Arguments.of("iconUrl", INVALID_URL)
        );
    }
}
