package com.example.product_service.service.util;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.entity.Products;
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
        ProductCreationData data = new ProductCreationData(
                createCategory(1L),
                createOptionTypeById(1L),
                createOptionValueById(1L)
        );

        Products product = factory.createProducts(request, data);

        assertThat(product.getName()).isEqualTo("name");
        assertThat(product.getDescription()).isEqualTo("description");
        assertThat(product.getCategory().getId()).isEqualTo(1L);

        assertThat(product.getImages())
                .extracting("imageUrl", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://test.jpg", 0)
                );

        assertThat(product.getProductOptionTypes()).hasSize(1);

        assertThat(product.getProductOptionTypes().get(0).getOptionType().getId()).isEqualTo(1L);
        assertThat(product.getProductOptionTypes().get(0).getOptionType().getName()).isEqualTo("optionType1");

        assertThat(product.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountValue")
                .containsExactlyInAnyOrder(
                        tuple("sku", 1000, 100, 10)
                );

        assertThat(product.getProductVariants().get(0).getProductVariantOptions()).hasSize(1);

        assertThat(product.getProductVariants().get(0).getProductVariantOptions().get(0).getOptionValue().getId())
                .isEqualTo(1L);
        assertThat(product.getProductVariants().get(0).getProductVariantOptions().get(0).getOptionValue().getValueName())
                .isEqualTo("optionValue1");
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg")),
                List.of(new ProductOptionTypeRequest(1L, 1)),
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

    private Map<Long, OptionTypes> createOptionTypeById(Long... ids){
        Map<Long, OptionTypes> map = new HashMap<>();
        for (Long id : ids) {
            OptionTypes optionType = new OptionTypes("optionType" + id);
            ReflectionTestUtils.setField(optionType, "id", id);
            map.put(id, optionType);
        }
        return map;
    }

    private Map<Long, OptionValues> createOptionValueById(Long... ids){
        Map<Long, OptionValues> map = new HashMap<>();
        for (Long id : ids) {
            OptionValues optionValue = new OptionValues("optionValue" + id);
            ReflectionTestUtils.setField(optionValue, "id", id);
            map.put(id, optionValue);
        }
        return map;
    }
}