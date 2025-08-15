package com.example.product_service.service;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.util.ProductFactory;
import com.example.product_service.service.util.ProductReferentialValidator;
import com.example.product_service.service.util.ProductRequestStructureValidator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Slf4j
class ProductServiceTest {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductService productService;

    @Autowired
    EntityManager em;

    OptionTypes existType;
    OptionValues existValue;
    Categories category;

    @BeforeEach
    void saveFixture(){
        existType = new OptionTypes("optionType");
        existValue = new OptionValues("optionValue");
        category = new Categories("category", "http://test.jpg");
        existType.addOptionValue(existValue);
        optionTypeRepository.save(existType);
        categoryRepository.save(category);
    }

    @AfterEach
    void clearDB(){
        productsRepository.deleteAll();
        categoryRepository.deleteAll();
        optionTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 저장 테스트-성공")
    @Transactional
    void saveProduct_integration_success(){
        ProductRequest request = new ProductRequest("productName", "product description", category.getId(),
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(existType.getId(), 0)),
                List.of(new ProductVariantRequest("sku", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(existType.getId(), existValue.getId()))))
        );
        ProductResponse response = productService.saveProduct(request);

        assertThat(response)
                .extracting("name", "description", "categoryId")
                .containsExactly("productName", "product description", category.getId());

        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                        .containsExactlyInAnyOrder(
                                tuple("http://test.jpg", 0)
                        );

        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(existType.getId(), existType.getName())
                );

        assertThat(response.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("sku", 100, 10, 10)
                );

        ProductVariantResponse variant = response.getProductVariants().stream()
                .filter(v -> "sku".equals(v.getSku()))
                .findFirst()
                .orElseThrow();

        assertThat(variant.getOptionValues()).hasSize(1);
        assertThat(variant.getOptionValues())
                .extracting("valueId", "typeId", "valueName")
                .containsExactly(tuple(existValue.getId(), existType.getId(), existValue.getValueName()));
    }
}