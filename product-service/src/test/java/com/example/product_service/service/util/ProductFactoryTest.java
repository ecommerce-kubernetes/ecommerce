package com.example.product_service.service.util;

import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.service.dto.ProductCreationCommand;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCommand;
import com.example.product_service.service.dto.ProductVariantCreationData;
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
    @DisplayName("Product 생성 테스트")
    void createProductsTest(){
        ProductRequest request = createProductRequest();
        ProductCreationCommand command = new ProductCreationCommand(request);
        OptionType optionType = createOptionType(1L, "optionType");
        OptionValue optionValue = createOptionValue(1L, "optionValue", optionType);

        Map<Long, OptionType> optionTypeById = new HashMap<>();
        optionTypeById.put(optionType.getId(), optionType);

        Map<Long, OptionValue> optionValueById = new HashMap<>();
        optionValueById.put(optionValue.getId(), optionValue);

        ProductCreationData data = new ProductCreationData(
                createCategory(1L),
                optionTypeById,
                optionValueById
        );

        Product product = factory.createProducts(command, data);

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
                        ProductOptionType::getPriority,
                        ProductOptionType::isActive)
                .containsExactlyInAnyOrder(
                        tuple(1L, "optionType", 0, true)
                );

        assertThat(product.getProductVariants())
                .extracting(ProductVariant::getSku,
                        ProductVariant::getPrice,
                        ProductVariant::getStockQuantity,
                        ProductVariant::getDiscountValue)
                .containsExactlyInAnyOrder(
                        tuple("sku", 1000, 100, 10)
                );

        assertThat(product.getProductVariants())
                .flatExtracting(ProductVariant::getProductVariantOptions)
                .extracting(pvo -> pvo.getOptionValue().getOptionValue())
                .containsExactlyInAnyOrder( "optionValue");
    }

    @Test
    @DisplayName("ProductVariant 생성 테스트")
    void createProductVariantTest(){
        ProductVariantRequest request = createProductVariantRequest();
        ProductVariantCommand command = new ProductVariantCommand(request);

        OptionType optionType = createOptionType(1L, "optionType");
        OptionValue optionValue = createOptionValue(1L, "optionValue", optionType);

        Map<Long, OptionValue> optionValueById = new HashMap<>();
        optionValueById.put(optionValue.getId(), optionValue);
        ProductVariantCreationData creationData = new ProductVariantCreationData(optionValueById);

        ProductVariant productVariant = factory.createProductVariant(command, creationData);

        assertThat(productVariant.getSku()).isEqualTo("sku");
        assertThat(productVariant.getPrice()).isEqualTo(10000);
        assertThat(productVariant.getStockQuantity()).isEqualTo(1000);
        assertThat(productVariant.getDiscountValue()).isEqualTo(10);
        assertThat(productVariant.getProductVariantOptions())
                .extracting(pvo -> pvo.getOptionValue().getOptionValue())
                .containsExactlyInAnyOrder("optionValue");

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

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku",
                10000,
                1000,
                10,
                List.of(new VariantOptionValueRequest(1L, 1L))
        );
    }

    private Category createCategory(Long id){
        Category category = new Category("category", "http://test.jpg");
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private OptionType createOptionType(Long id, String name){
        OptionType optionType = new OptionType(name);
        ReflectionTestUtils.setField(optionType, "id", id);
        return optionType;
    }

    private OptionValue createOptionValue(Long id, String name, OptionType optionType){
        OptionValue optionValue = new OptionValue(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        optionType.addOptionValue(optionValue);
        return optionValue;
    }
}