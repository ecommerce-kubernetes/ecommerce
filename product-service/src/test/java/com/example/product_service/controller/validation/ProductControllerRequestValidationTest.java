package com.example.product_service.controller.validation;

import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.controller.util.ValidationTestHelper;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

import static com.example.product_service.controller.util.ValidationTestHelper.*;

@ExtendWith(SpringExtension.class)
public class ProductControllerRequestValidationTest {

    @ParameterizedTest(name = "[{index}] {0} 필드 invalid")
    @MethodSource("invalidProductRequestFieldProvider")
    void productRequestValidation_field(String fieldName, Object invalidValue, String expectedMessage){
        ProductRequest request = new ProductRequest("name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                List.of(new ProductVariantRequest(
                        "sku", 100, 100, 10,
                        List.of(1L, 2L)
                )));

        assertFieldViolation(request, fieldName, invalidValue, expectedMessage);
    }


    static Stream<Arguments> invalidProductRequestFieldProvider(){
        return Stream.of(
                Arguments.of("name", "", TestMessageUtil.getMessage("product.name.notBlank"))
        );
    }
}
