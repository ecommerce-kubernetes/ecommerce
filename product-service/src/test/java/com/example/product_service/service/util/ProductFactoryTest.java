package com.example.product_service.service.util;

import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.service.dto.ProductCreationCommand;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {

    @InjectMocks
    ProductFactory factory;

    @Test
    @DisplayName("Factory 동작 테스트")
    void createProductsTest(){
        ProductRequest request = createProductRequest();
        OptionTypes optionType = createOptionType(1L, "optionType");
        OptionValues optionValue = createOptionValue(1L, "optionValue", optionType);

        Map<Long, OptionTypes> optionTypeById = new HashMap<>();
        optionTypeById.put(optionType.getId(), optionType);

        Map<Long, OptionValues> optionValueById = new HashMap<>();
        optionValueById.put(optionValue.getId(), optionValue);

        ProductCreationData data = new ProductCreationData(
                createCategory(1L),
                optionTypeById,
                optionValueById
        );

        Products product = factory.createProducts(request, data);

        assertThat(product.getName()).isEqualTo("name");
        assertThat(product.getDescription()).isEqualTo("description");
        assertThat(product.getCategory().getId()).isEqualTo(1L);

        assertThat(product.getImages())
                .extracting("imageUrl", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://test.jpg", 0),
                        tuple("http://test2.jpg", 1)
                );

        assertThat(product.getProductOptionTypes()).hasSize(1);

        assertThat(product.getProductOptionTypes().get(0).getOptionType().getId()).isEqualTo(1L);
        assertThat(product.getProductOptionTypes().get(0).getOptionType().getName()).isEqualTo("optionType");

        assertThat(product.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountValue")
                .containsExactlyInAnyOrder(
                        tuple("sku", 1000, 100, 10)
                );

        assertThat(product.getProductVariants().get(0).getProductVariantOptions()).hasSize(1);

        assertThat(product.getProductVariants().get(0).getProductVariantOptions().get(0).getOptionValue().getId())
                .isEqualTo(1L);
        assertThat(product.getProductVariants().get(0).getProductVariantOptions().get(0).getOptionValue().getOptionValue())
                .isEqualTo("optionValue");
    }

    @Test
    @DisplayName("Factory 동작 테스트")
    void createProductsTest_v2(){
        ProductRequest request = createProductRequest();
        ProductCreationCommand command = new ProductCreationCommand(request);
        OptionTypes optionType = createOptionType(1L, "optionType");
        OptionValues optionValue = createOptionValue(1L, "optionValue", optionType);

        Map<Long, OptionTypes> optionTypeById = new HashMap<>();
        optionTypeById.put(optionType.getId(), optionType);

        Map<Long, OptionValues> optionValueById = new HashMap<>();
        optionValueById.put(optionValue.getId(), optionValue);

        ProductCreationData data = new ProductCreationData(
                createCategory(1L),
                optionTypeById,
                optionValueById
        );

        Products product = factory.createProducts(command, data);

        assertThat(product.getName()).isEqualTo("name");
        assertThat(product.getDescription()).isEqualTo("description");
        assertThat(product.getCategory().getId()).isEqualTo(1L);

        assertThat(product.getImages())
                .extracting("imageUrl", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://test.jpg", 0),
                        tuple("http://test2.jpg", 1)
                );

        assertThat(product.getProductOptionTypes())
                .extracting(pot -> pot.getOptionType().getId(),
                        pot -> pot.getOptionType().getName(),
                        ProductOptionTypes::getPriority,
                        ProductOptionTypes::isActive)
                .containsExactlyInAnyOrder(
                        tuple(1L, "optionType", 0, true)
                );

        assertThat(product.getProductVariants())
                .extracting(ProductVariants::getSku,
                        ProductVariants::getPrice,
                        ProductVariants::getStockQuantity,
                        ProductVariants::getDiscountValue)
                .containsExactlyInAnyOrder(
                        tuple("sku", 1000, 100, 10)
                );

        assertThat(product.getProductVariants())
                .flatExtracting(ProductVariants::getProductVariantOptions)
                .extracting(pvo -> pvo.getOptionValue().getOptionValue())
                .containsExactlyInAnyOrder( "optionValue");
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of("http://test.jpg", "http://test2.jpg"),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                List.of(new ProductVariantRequest("sku", 1000, 100, 10,
                        List.of(
                                new VariantOptionValueRequest(1L, 1L)
                        )))

        );
    }

    private Categories createCategory(Long id){
        Categories category = new Categories("category", "http://test.jpg");
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private OptionTypes createOptionType(Long id, String name){
        OptionTypes optionTypes = new OptionTypes(name);
        ReflectionTestUtils.setField(optionTypes, "id", id);
        return optionTypes;
    }

    private OptionValues createOptionValue(Long id, String name, OptionTypes optionTypes){
        OptionValues optionValues = new OptionValues(name);
        ReflectionTestUtils.setField(optionValues, "id", id);
        optionTypes.addOptionValue(optionValues);
        return optionValues;
    }
}